package ir.erfansn.nsmavpn.feature.home.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.core.AndroidString
import ir.erfansn.nsmavpn.data.repository.ConfigurationsRepository
import ir.erfansn.nsmavpn.data.repository.LastVpnConnectionRepository
import ir.erfansn.nsmavpn.data.repository.NoAvailableVpnServerException
import ir.erfansn.nsmavpn.data.repository.VpnServersRepository
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.OscPrefKey
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.control.ControlClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface SstpVpnEventHandler {
    val connectionState: SharedFlow<ConnectionState>
    fun initState()
    context(SstpVpnService) suspend fun startConnecting()
    fun disconnectVpnDue(error: ConnectionState.Error)
    suspend fun shutdownVpnTunnel(quietly: Boolean)
    fun cleanUp()
}

class DefaultSstpVpnEventHandler @Inject constructor(
    private val vpnServersRepository: VpnServersRepository,
    private val configurationsRepository: ConfigurationsRepository,
    private val sstpVpnServiceAction: SstpVpnServiceAction,
    private val lastVpnConnectionRepository: LastVpnConnectionRepository,
    private val okHttpClient: OkHttpClient,
    private val applicationScope: CoroutineScope,
    @ApplicationContext private val context: Context,
) : SstpVpnEventHandler {

    private var controlClient: ControlClient? = null
    private lateinit var currentServer: Server

    private var validationJob: Job? = null

    private val _connectionState = MutableSharedFlow<ConnectionState>(replay = 1)
    override val connectionState = _connectionState.asSharedFlow()

    private val sstpVpnServiceActionDelegator = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_DELEGATOR_VPN_DISCONNECT) {
                sstpVpnServiceAction.disconnect()
            }
        }
    }

    init {
        ContextCompat.registerReceiver(
            context,
            sstpVpnServiceActionDelegator,
            IntentFilter(ACTION_DELEGATOR_VPN_DISCONNECT),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun initState() {
        _connectionState.tryEmit(ConnectionState.Connecting)
    }

    context(SstpVpnService)
    override suspend fun startConnecting() {
        controlClient?.cleanUp()
        currentServer = (vpnServersRepository.tryToGetLastVpnConnectionServer() ?: obtainVpnServer()).also {
            OscPrefKey.HOME_HOSTNAME = it.address.hostName
            OscPrefKey.SSL_PORT = it.address.portNumber
        }
        initializeClient()
    }

    private suspend fun obtainVpnServer(): Server {
        return try {
            vpnServersRepository.findFastestSstpVpnServer()
        } catch (e: NoAvailableVpnServerException) {
            vpnServersRepository.unblockReachableVpnServers()
            obtainVpnServer()
        }
    }

    private suspend fun SstpVpnService.initializeClient() {
        OscPrefKey.DISALLOWED_APPS = configurationsRepository.configurations.first().splitTunnelingAppIds

        controlClient = ControlClient(
            bridge = ClientBridge(this),
            onRestartVpn = ::restartVpn,
            onStartConnectionValidation = ::startConnectionValidation,
            onCancelConnectionValidation = ::cancelConnectionValidation,
        ).also {
            it.launchJobMain()
        }
    }

    private fun startConnectionValidation() {
        cancelConnectionValidation()
        validationJob = applicationScope.launch {
            _connectionState.emit(ConnectionState.Validating)

            if (vpnGateContentReachable()) {
                lastVpnConnectionRepository.saveLastConnectionInfo(
                    vpnServer = currentServer,
                    connectionTimeSinceEpoch = System.currentTimeMillis(),
                )
                _connectionState.emit(ConnectionState.Connected(CountryCode(currentServer.countryCode)))
            } else {
                restartVpn()
            }
        }
    }

    private fun cancelConnectionValidation() {
        validationJob?.cancel()
    }

    private suspend fun vpnGateContentReachable() = runInterruptible(Dispatchers.IO) {
        var result = false
        for (n in 1..3) {
            val networkCallResult = runCatching {
                val request = Request.Builder()
                    .url("https://www.vpngate.net/")
                    .build()
                okHttpClient.newBuilder()
                    .callTimeout(6, TimeUnit.SECONDS)
                    .build()
                    .newCall(request)
                    .execute()
                    .close()
            }
            if (networkCallResult.isSuccess) {
                result = true
                println("Hello")
                break
            }
        }
        result
    }

    private suspend fun restartVpn() {
        if (!mutex.tryLock()) return
        vpnServersRepository.blockVpnServer(currentServer)
        sstpVpnServiceAction.connect()
        mutex.unlock()
    }

    override fun disconnectVpnDue(error: ConnectionState.Error) {
        _connectionState.tryEmit(error)
        sstpVpnServiceAction.disconnect(quietly = true)
    }

    override suspend fun shutdownVpnTunnel(quietly: Boolean) {
        if (!quietly) _connectionState.emit(ConnectionState.Disconnecting)
        validationJob?.cancelAndJoin()

        controlClient?.disconnect()
        controlClient = null
        if (!quietly) _connectionState.emit(ConnectionState.Disconnected)
    }

    override fun cleanUp() {
        controlClient?.cleanUp()
        controlClient = null

        context.unregisterReceiver(sstpVpnServiceActionDelegator)
    }

    companion object {
        const val ACTION_DELEGATOR_VPN_DISCONNECT = "delegator_vpn_disconnect"

        private val mutex = Mutex()
    }
}

sealed class ConnectionState(val message: AndroidString) {
    data object Connecting : ConnectionState(AndroidString(R.string.connecting))
    data class Connected(val serverCountryCode: CountryCode) : ConnectionState(AndroidString(R.string.connected))
    data object Disconnecting : ConnectionState(AndroidString(R.string.disconnecting))
    data object Disconnected : ConnectionState(AndroidString(R.string.disconnected))
    data object Validating : ConnectionState(AndroidString(R.string.validating))
    sealed class Error(errorMessage: AndroidString) : ConnectionState(errorMessage) {
        data object Network : Error(AndroidString(R.string.network_error))
        data object System : Error(AndroidString(R.string.deactivated))
    }
}

@JvmInline
value class CountryCode(val value: String) {
    init {
        require(value in Locale.getISOCountries())
    }
}
