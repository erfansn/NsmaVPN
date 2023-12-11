package ir.erfansn.nsmavpn.feature.home.vpn

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.repository.ConfigurationsRepository
import ir.erfansn.nsmavpn.data.repository.LastVpnConnectionRepository
import ir.erfansn.nsmavpn.data.repository.NoAvailableVpnServerException
import ir.erfansn.nsmavpn.data.repository.ServersRepository
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.util.PingChecker
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.OscPrefKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

class DefaultSstpVpnEventHandler(
    private val pingChecker: PingChecker,
    private val lastVpnConnectionRepository: LastVpnConnectionRepository,
    private val serversRepository: ServersRepository,
    private val configurationsRepository: ConfigurationsRepository,
    private val context: Context,
) : SstpVpnEventHandler {

    override val connectionState = MutableSharedFlow<ConnectionState>(replay = 1)

    private lateinit var currentServer: Server

    override suspend fun obtainVpnServer(): Server {
        return try {
            serversRepository.obtainFastestVpnServer()
        } catch (e: NoAvailableVpnServerException) {
            serversRepository.unblockReachableVpnServers()
            delay(100.milliseconds)
            obtainVpnServer()
        }.also {
            currentServer = it
        }
    }

    override fun onConnectionAbort() {
        notifyConnecting()
    }

    override fun notifyConnecting() {
        connectionState.tryEmit(ConnectionState.Connecting)
    }

    override fun notifyDisconnecting() {
        connectionState.tryEmit(ConnectionState.Disconnecting)
    }

    override fun notifyDisconnected() {
        connectionState.tryEmit(ConnectionState.Disconnected)
    }

    override fun notifyNetworkError() {
        connectionState.tryEmit(ConnectionState.NetworkError)
    }

    override suspend fun startConnectionValidation() {
        connectionState.tryEmit(ConnectionState.Validating)

        if (pingChecker.isReachable()) {
            lastVpnConnectionRepository.saveLastConnectionInfo(
                vpnServer = currentServer,
                connectionTimeSinceEpoch = System.currentTimeMillis(),
            )
            notifyConnected()
        } else {
            restartService()
        }
    }

    private fun notifyConnected() {
        connectionState.tryEmit(ConnectionState.Connected(CountryCode(currentServer.countryCode)))
    }

    override fun restartService() {
        ContextCompat.startForegroundService(
            context,
            Intent(context, SstpVpnService::class.java).apply {
                action = SstpVpnService.ACTION_VPN_CONNECT
                putExtra(SstpVpnService.EXTRA_RESTART, true)
            }
        )
    }

    override suspend fun blockCurrentServer() {
        serversRepository.blockVpnServer(
            server = currentServer
        )
    }

    override fun disconnectServiceDueNetworkError() {
        connectionState.tryEmit(ConnectionState.NetworkError)

        context.startService(
            Intent(context, SstpVpnService::class.java).apply {
                action = SstpVpnService.ACTION_VPN_DISCONNECT
                putExtra(SstpVpnService.EXTRA_SILENT, true)
            }
        )
    }

    override suspend fun updateDisallowedAppsIdList() {
        OscPrefKey.DISALLOWED_APPS = configurationsRepository.configurations.map {
            it.splitTunnelingAppIds
        }.first()
    }
}

interface SstpVpnEventHandler {
    val connectionState: Flow<ConnectionState>
    suspend fun obtainVpnServer(): Server
    suspend fun blockCurrentServer()
    fun onConnectionAbort()
    fun notifyConnecting()
    fun notifyDisconnecting()
    fun notifyDisconnected()
    fun notifyNetworkError()
    suspend fun startConnectionValidation()
    fun restartService()
    fun disconnectServiceDueNetworkError()
    suspend fun updateDisallowedAppsIdList()
}

sealed class ConnectionState(@StringRes val messageId: Int) {
    data object Connecting : ConnectionState(R.string.connecting)
    data class Connected(val serverCountryCode: CountryCode) : ConnectionState(R.string.connected)
    data object Disconnecting : ConnectionState(R.string.disconnecting)
    data object Disconnected : ConnectionState(R.string.disconnected)
    data object Validating : ConnectionState(R.string.validating)
    data object NetworkError : ConnectionState(R.string.network_error)
}

@JvmInline
value class CountryCode(val value: String) {
    init {
        require(value in Locale.getISOCountries())
    }
}
