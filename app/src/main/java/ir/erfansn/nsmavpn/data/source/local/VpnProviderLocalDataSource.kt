package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnProvider
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DefaultVpnProviderLocalDataSource @Inject constructor(
    private val dataStore: DataStore<VpnProvider>,
) : VpnProviderLocalDataSource {

    override suspend fun getVpnServers(): List<Server> {
        return dataStore.data.first().serversList
    }

    override suspend fun getVpnProviderAddress() =
        dataStore.data.first().address!!

    override suspend fun updateVpnProviderAddress(address: String) {
        dataStore.updateData {
            it.toBuilder().setAddress(address).build()
        }
    }

    override suspend fun saveVpnServers(servers: List<Server>) {
        dataStore.updateData { vpnProvider ->
            vpnProvider.toBuilder().apply {
                val newServers = servers.filter { it !in vpnProvider.serversList }
                addAllServers(newServers)
            }.build()
        }
    }
}

interface VpnProviderLocalDataSource {
    suspend fun getVpnServers(): List<Server>
    suspend fun getVpnProviderAddress(): String
    suspend fun updateVpnProviderAddress(address: String)
    suspend fun saveVpnServers(servers: List<Server>)
}
