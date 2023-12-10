package ir.erfansn.nsmavpn.feature.home.vpn

import android.Manifest
import android.app.ActivityOptions
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.core.application.MainActivity
import ir.erfansn.nsmavpn.ui.util.toCountryName

interface SstpVpnNotificationManager {
    fun updateNotification(connectionState: ConnectionState)
    fun initialNotification(): Notification
}

class DefaultSstpVpnNotificationManager(
    private val context: Context,
    private val targetNotificationId: Int,
) : SstpVpnNotificationManager {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName("Connection state")
            .setShowBadge(false)
            .build()

        notificationManager.createNotificationChannel(channel)
    }

    override fun updateNotification(connectionState: ConnectionState) {
        val notification = buildNotificationByState(connectionState)
        updateNotification(notification)
    }

    override fun initialNotification() =
        buildNotificationByState(ConnectionState.Connecting)

    private fun buildNotificationByState(connectionState: ConnectionState): Notification {
        val actions = when (connectionState) {
            ConnectionState.Validating,
            ConnectionState.Connecting,
            is ConnectionState.Connected
            -> listOf(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_baseline_close_24,
                    "Disconnect",
                    PendingIntentCompat.getService(
                        context,
                        0,
                        Intent(context, SstpVpnService::class.java).apply {
                            action = SstpVpnService.ACTION_VPN_DISCONNECT
                        },
                        PendingIntent.FLAG_ONE_SHOT,
                        false,
                    )
                ).build()
            )
            ConnectionState.Disconnected -> listOf(
                NotificationCompat.Action.Builder(
                    null,
                    "Connect",
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        PendingIntentCompat::getForegroundService
                    } else {
                        PendingIntentCompat::getService
                    }.invoke(
                        context,
                        0,
                        Intent(context, SstpVpnService::class.java).apply {
                            action = SstpVpnService.ACTION_VPN_CONNECT
                        },
                        PendingIntent.FLAG_ONE_SHOT,
                        false
                    )
                ).build()
            )
            ConnectionState.NetworkError,
            ConnectionState.Disconnecting
            -> emptyList()
        }
        val contentText = when (connectionState) {
            is ConnectionState.Connected -> context.getString(connectionState.messageId, connectionState.serverCountryCode.toCountryName())
            else -> context.getString(connectionState.messageId)
        }

        return buildPlatformNotification(contentText, actions)
    }

    private fun buildPlatformNotification(
        contentText: String,
        actions: List<NotificationCompat.Action> = listOf()
    ): Notification {
        val launchAppPendingIntent = PendingIntentCompat.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT,
            ActivityOptions.makeBasic().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                }
            }.toBundle(),
            false
        )

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).apply {
            setContentTitle("VPN status")
            setContentText(contentText)
            setContentIntent(launchAppPendingIntent)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setCategory(NotificationCompat.CATEGORY_SERVICE)
            setSmallIcon(R.drawable.ic_baseline_vpn_lock_24)
            actions.forEach(::addAction)
        }.build()
    }

    private fun updateNotification(notification: Notification) {
        // Because we don't want to drop notification updates
        mainHandler.post {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(targetNotificationId, notification)
            }
        }
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "vpn_state"
    }
}
