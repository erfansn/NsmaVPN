package ir.erfansn.nsmavpn.feature.home.vpn.service

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Parcel
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.model.NetworkUsage
import ir.erfansn.nsmavpn.data.util.NetworkUsageTracker
import ir.erfansn.nsmavpn.data.util.monitor.VpnConnectionStatus
import ir.erfansn.nsmavpn.data.util.monitor.VpnNetworkMonitor
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.control.ControlClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SstpVpnService : VpnService() {

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var vpnNetworkMonitor: VpnNetworkMonitor

    @Inject
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var networkUsageTracker: NetworkUsageTracker

    private var controlClient: ControlClient? = null
    private val vpnStateExposer = VpnStateExposer()

    override fun onCreate() {
        createVpnNotificationChannel()

        notificationManager = NotificationManagerCompat.from(this)
        vpnNetworkMonitor = VpnNetworkMonitor(this)

        scope.launch {
            var dataUsageTrackerJob: Job? = null

            vpnNetworkMonitor.status.collect {
                when (it) {
                    VpnConnectionStatus.Established -> {
                        dataUsageTrackerJob = launch {
                            networkUsageTracker.trackUsage(0).collect { data ->
                                vpnStateExposer.onEstablishment?.invoke(data)
                            }
                        }
                    }
                    VpnConnectionStatus.Invalid -> {
                        vpnStateExposer.onInvalid?.invoke()
                    }
                    VpnConnectionStatus.Unknown -> {
                        dataUsageTrackerJob?.cancel()
                        dataUsageTrackerJob = null
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_VPN_CONNECT -> {
                startVpnForegroundService()
                // TODO: Implement it inside DataStore after 3 reconnection reset it to 3 and block
                //  The server

                initializeClient()

                Service.START_STICKY
            }
            ACTION_VPN_DISCONNECT -> {
                scope.launch {
                    controlClient?.disconnect()
                    controlClient?.kill(false, null)
                    controlClient = null
                }
                Service.START_NOT_STICKY
            }
            else -> super.onStartCommand(intent, flags, startId)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return vpnStateExposer
    }

    inner class VpnStateExposer : Binder() {
        var onEstablishment: ((NetworkUsage) -> Unit)? = null
        var onInvalid: (() -> Unit)? = null

        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code == LAST_CALL_TRANSACTION) {
                onRevoke()
                return true
            }
            return false
        }
    }

    private fun initializeClient() {
        controlClient = ControlClient(ClientBridge(this)).also {
            it.launchJobMain()
        }
    }

    fun launchJobReconnect() = Unit

    private fun startVpnForegroundService() {
        val pendingIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, SstpVpnService::class.java).setAction(ACTION_VPN_DISCONNECT),
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .addAction(R.drawable.ic_launcher_background, "DISCONNECT", pendingIntent)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createVpnNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL_NAME, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName("VPN status")
            .setDescription("Shows whether is connection on what status")
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    fun makeNotification(id: Int, message: String) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }
        notificationManager.notify(id, notification)
    }

    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    fun close() {
        // TODO: only detach and provide a action button to reconnect
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    override fun onDestroy() {
        controlClient?.kill(false, null)
        controlClient = null
    }

    companion object {
        const val ACTION_VPN_CONNECT = "ir.erfansn.nsmavpn.action.VPN_CONNECT"
        const val ACTION_VPN_DISCONNECT = "ir.erfansn.nsmavpn.action.VPN_DISCONNECT"

        private const val NOTIFICATION_CHANNEL_NAME = "vpn_state"
        private const val NOTIFICATION_ID = 1
    }
}
