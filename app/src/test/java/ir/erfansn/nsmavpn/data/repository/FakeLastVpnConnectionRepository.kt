package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.source.local.datastore.LastVpnConnection
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.lastVpnConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeLastVpnConnectionRepository : LastVpnConnectionRepository {

    private lateinit var desiredLastVpnConnection: LastVpnConnection

    override val lastVpnConnection: Flow<LastVpnConnection>
        get() = flowOf(desiredLastVpnConnection)

    override suspend fun saveLastConnectionInfo(vpnServer: Server, connectionTimeSinceEpoch: Long) {
        desiredLastVpnConnection = lastVpnConnection {
            this.server = vpnServer
            this.epochTime = connectionTimeSinceEpoch
        }
    }
}
