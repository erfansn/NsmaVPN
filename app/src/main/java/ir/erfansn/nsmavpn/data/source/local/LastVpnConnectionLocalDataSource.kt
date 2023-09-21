package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.source.local.datastore.LastVpnConnection
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.copy
import javax.inject.Inject

class DefaultLastVpnConnectionLocalDataSource @Inject constructor(
    private val dataStore: DataStore<LastVpnConnection>
) : LastVpnConnectionLocalDataSource {

    override suspend fun saveLastConnectionInfo(vpnServer: Server, connectionTimeSinceEpoch: Long) {
        dataStore.updateData {
            it.copy {
                server = vpnServer
                epochTime = connectionTimeSinceEpoch
            }
        }
    }
}

interface LastVpnConnectionLocalDataSource {
    suspend fun saveLastConnectionInfo(vpnServer: Server, connectionTimeSinceEpoch: Long)
}
