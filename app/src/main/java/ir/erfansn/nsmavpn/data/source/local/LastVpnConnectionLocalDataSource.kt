package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.source.local.datastore.LastVpnConnection
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.copy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultLastVpnConnectionLocalDataSource @Inject constructor(
    private val dataStore: DataStore<LastVpnConnection>
) : LastVpnConnectionLocalDataSource {

    override val lastVpnConnection: Flow<LastVpnConnection> = dataStore.data

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
    val lastVpnConnection: Flow<LastVpnConnection>
    suspend fun saveLastConnectionInfo(vpnServer: Server, connectionTimeSinceEpoch: Long)
}
