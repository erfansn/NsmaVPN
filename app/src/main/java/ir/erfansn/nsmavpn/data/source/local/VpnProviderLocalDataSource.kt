package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnProvider
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class VpnProviderLocalDataSource @Inject constructor(
    private val dataStore: DataStore<VpnProvider>,
) {
    suspend fun getVpnProviderAddress() =
        dataStore.data.first().address!!

    suspend fun updateVpnProviderAddress(address: String) {
        dataStore.updateData {
            it.toBuilder().setAddress(address).build()
        }
    }

    suspend fun saveVpnServers(servers: List<Server>) {
        dataStore.updateData { vpnProvider ->
            vpnProvider.toBuilder().apply {
                val newServers = servers.filter { it in vpnProvider.serversList }
                addAllServers(newServers)
            }.build()
        }
    }
}