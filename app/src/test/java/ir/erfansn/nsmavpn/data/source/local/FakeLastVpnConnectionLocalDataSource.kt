package ir.erfansn.nsmavpn.data.source.local

import ir.erfansn.nsmavpn.data.source.local.datastore.LastVpnConnection
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.lastVpnConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeLastVpnConnectionLocalDataSource : LastVpnConnectionLocalDataSource {

    private lateinit var lastServer: Server

    override val lastVpnConnection: Flow<LastVpnConnection>
        get() = flowOf(
            lastVpnConnection {
                this.server = this@FakeLastVpnConnectionLocalDataSource.lastServer
            }
        )

    override suspend fun saveLastConnectionInfo(vpnServer: Server, connectionTimeSinceEpoch: Long) {
        lastServer = vpnServer
    }
}