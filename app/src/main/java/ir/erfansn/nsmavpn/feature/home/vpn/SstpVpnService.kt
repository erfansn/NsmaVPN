package ir.erfansn.nsmavpn.feature.home.vpn

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.erfansn.nsmavpn.data.repository.ConfigurationsRepository
import ir.erfansn.nsmavpn.data.repository.LastVpnConnectionRepository
import ir.erfansn.nsmavpn.data.repository.ServersRepository
import ir.erfansn.nsmavpn.data.util.NetworkMonitor
import ir.erfansn.nsmavpn.data.util.PingChecker
import ir.erfansn.nsmavpn.data.util.asyncMap
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.OscPrefKey
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.control.ControlClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SstpVpnService : VpnService() {

    private val TAG: String? = this::class.simpleName
    lateinit var serviceScope: CoroutineScope
    private var controlClient: ControlClient? = null

    private var vpnStartingJob: Job? = null
    private var jobReconnect: Job? = null
    private var disconnectJob: Job? = null
    private var validationJob: Job? = null

    @Inject
    lateinit var pingChecker: PingChecker
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    @Inject
    lateinit var lastVpnConnectionRepository: LastVpnConnectionRepository
    @Inject
    lateinit var serversRepository: ServersRepository

    private lateinit var sstpVpnEventHandler: SstpVpnEventHandler
    private lateinit var sstpVpnNotificationManager: SstpVpnNotificationManager
    private lateinit var localBinder: LocalBinder

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        sstpVpnEventHandler = DefaultSstpVpnEventHandler(
            pingChecker,
            lastVpnConnectionRepository,
            serversRepository,
            this,
        )
        sstpVpnNotificationManager = DefaultSstpVpnNotificationManager(
            this,
            FOREGROUND_NOTIFICATION_ID
        )
        localBinder = LocalBinder()

        sstpVpnEventHandler.connectionState
            .onEach(sstpVpnNotificationManager::updateNotification)
            .launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_VPN_CONNECT -> {
                disconnectJob?.cancel()
                controlClient?.cleanUp()
                notifyForeground()

                sstpVpnEventHandler.notifyConnecting()

                vpnStartingJob = networkMonitor.isOnline
                    .onEachLatest { isOnline ->
                        check(isOnline)

                        if (intent.getBooleanExtra(EXTRA_RESTART, false)) sstpVpnEventHandler.blockCurrentServer()
                        val server = sstpVpnEventHandler.obtainVpnServer()

                        OscPrefKey.HOME_HOSTNAME = server.address.hostName
                        OscPrefKey.SSL_PORT = server.address.portNumber
                        OscPrefKey.RECONNECTION_LIFE = OscPrefKey.RECONNECTION_COUNT

                        initializeClient()
                    }
                    .catch {
                        sstpVpnEventHandler.disconnectServiceDueNetworkError()
                    }
                    .launchIn(serviceScope)

                Service.START_REDELIVER_INTENT
            }

            ACTION_VPN_DISCONNECT -> {
                val silent = intent.getBooleanExtra(EXTRA_SILENT, false)

                if (!silent) sstpVpnEventHandler.notifyDisconnecting()
                disconnectJob = serviceScope.launch {
                    listOf(jobReconnect, vpnStartingJob, validationJob).asyncMap {
                        it?.cancelAndJoin()
                    }

                    controlClient?.disconnect()
                    controlClient = null

                    if (!silent) sstpVpnEventHandler.notifyDisconnected()
                    stopService()
                }
                Service.START_NOT_STICKY
            }

            else -> {
                throw IllegalStateException()
            }
        }
    }

    fun startConnectionValidation() {
        cancelConnectionValidation()
        validationJob = serviceScope.launch {
            sstpVpnEventHandler.startConnectionValidation()
        }
    }

    fun cancelConnectionValidation() {
        validationJob?.cancel()
    }

    fun onConnectionAbort() {
        sstpVpnEventHandler.onConnectionAbort()
    }

    fun restartService() {
        sstpVpnEventHandler.restartService()
    }

    private fun initializeClient() {
        controlClient = ControlClient(ClientBridge(this)).also {
            it.launchJobMain()
        }
    }

    fun launchJobReconnect() {
        jobReconnect = serviceScope.launch {
            OscPrefKey.RECONNECTION_LIFE.also {
                val life = it - 1
                OscPrefKey.RECONNECTION_LIFE = life
            }

            delay(OscPrefKey.RECONNECTION_INTERVAL * 1000L)

            initializeClient()
        }
    }

    @SuppressLint("InlinedApi")
    private fun notifyForeground() {
        ServiceCompat.startForeground(
            this,
            FOREGROUND_NOTIFICATION_ID,
            sstpVpnNotificationManager.initialNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        return super.onBind(intent) ?: localBinder
    }

    override fun onRevoke() {
        startService(
            Intent(this, SstpVpnService::class.java).apply {
                action = ACTION_VPN_DISCONNECT
            }
        )
    }

    fun stopService() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    override fun onDestroy() {
        controlClient?.cleanUp()
        controlClient = null

        serviceScope.cancel()
    }

    inner class LocalBinder : Binder() {
        val sstpVpnServiceState = this@SstpVpnService.sstpVpnEventHandler.connectionState.map {
            SstpVpnServiceState(connectionState = it)
        }
    }

    companion object {
        const val ACTION_VPN_CONNECT =
            "ir.erfansn.nsmavpn.feature.home.vpn.service.action.VPN_CONNECT"
        const val EXTRA_RESTART =
            "ir.erfansn.nsmavpn.feature.home.vpn.service.extra.RESTART"

        const val ACTION_VPN_DISCONNECT =
            "ir.erfansn.nsmavpn.feature.home.vpn.service.action.VPN_DISCONNECT"
        const val EXTRA_SILENT =
            "ir.erfansn.nsmavpn.feature.home.vpn.service.extra.SILENT"

        private const val FOREGROUND_NOTIFICATION_ID = 1
    }
}

data class SstpVpnServiceState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
) {
    val started: Boolean
        get() = connectionState is ConnectionState.Connected || connectionState in listOf(
            ConnectionState.Connecting,
            ConnectionState.Validating,
        )
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun <T> Flow<T>.onEachLatest(action: suspend (T) -> Unit): Flow<T> = transformLatest { value ->
    action(value)
    return@transformLatest emit(value)
}
