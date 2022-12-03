package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnProvider
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DefaultVpnProviderLocalDataSource @Inject constructor(
    private val dataStore: DataStore<VpnProvider>,
) : VpnProviderLocalDataSource {

    override suspend fun getVpnProviderAddress(): String =
        dataStore.data.first().address

    override suspend fun updateVpnProviderAddress(address: String) {
        dataStore.updateData {
            it.toBuilder().setAddress(address).build()
        }
    }

    override suspend fun getVpnServers(): List<Server> =
        dataStore.data.first().let { vpnProvider ->
            vpnProvider.serversList.filter { it !in vpnProvider.blackServersList }
        }

    override suspend fun saveVpnServers(servers: List<Server>) {
        dataStore.updateData { vpnProvider ->
            vpnProvider.toBuilder().apply {
                val newServers = servers.filter { it !in vpnProvider.serversList }
                addAllServers(newServers)
            }.build()
        }
    }

    override suspend fun getBlockedVpnServers(): List<Server> {
        return dataStore.data.first().blackServersList
    }

    override suspend fun blockVpnServers(servers: List<Server>) {
        dataStore.updateData { vpnProvider ->
            val newServers = servers.filter { it !in vpnProvider.blackServersList }
            vpnProvider.toBuilder().addAllBlackServers(newServers).build()
        }
    }

    override suspend fun unblockVpnServer(server: Server) {
        dataStore.updateData {
            check(server in it.blackServersList)

            val serverIndex = it.blackServersList.indexOf(server)
            it.toBuilder().removeBlackServers(serverIndex).build()
        }
    }
}

interface VpnProviderLocalDataSource {
    suspend fun getVpnProviderAddress(): String
    suspend fun updateVpnProviderAddress(address: String)
    suspend fun getVpnServers(): List<Server>
    suspend fun saveVpnServers(servers: List<Server>)
    suspend fun getBlockedVpnServers(): List<Server>
    suspend fun blockVpnServers(servers: List<Server>)
    suspend fun unblockVpnServer(server: Server)
}
