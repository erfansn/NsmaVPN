package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.source.local.LastVpnConnectionLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.LastVpnConnection
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultLastVpnConnectionRepository @Inject constructor(
    private val lastVpnConnectionLocalDataSource: LastVpnConnectionLocalDataSource
) : LastVpnConnectionRepository {

    override val lastVpnConnection: Flow<LastVpnConnection> =
        lastVpnConnectionLocalDataSource.lastVpnConnection

    override suspend fun saveLastConnectionInfo(vpnServer: Server, connectionTimeSinceEpoch: Long) {
        lastVpnConnectionLocalDataSource.saveLastConnectionInfo(vpnServer, connectionTimeSinceEpoch)
    }
}

interface LastVpnConnectionRepository {
    val lastVpnConnection: Flow<LastVpnConnection>
    suspend fun saveLastConnectionInfo(vpnServer: Server, connectionTimeSinceEpoch: Long)
}
