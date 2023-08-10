package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.UrlLink
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnProvider
import ir.erfansn.nsmavpn.data.source.local.datastore.copy
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Instead of using Impl suffix we use Default prefix because Impl suffix violates DRY principle.
 *
 * [Reference](https://link.medium.com/u5o2xd8Ahxb)
 */
class DefaultVpnProviderLocalDataSource @Inject constructor(
    private val dataStore: DataStore<VpnProvider>,
) : VpnProviderLocalDataSource {

    override suspend fun getVpnProviderAddress(): UrlLink =
        dataStore.data.first().address

    override suspend fun saveVpnProviderAddress(address: UrlLink) {
        dataStore.updateData {
            it.copy {
                this.address = address
            }
        }
    }

    override suspend fun getAvailableVpnServers(): List<Server> =
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

    override suspend fun blockVpnServers(vararg servers: Server) {
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
    suspend fun getVpnProviderAddress(): UrlLink
    suspend fun saveVpnProviderAddress(address: UrlLink)
    suspend fun getAvailableVpnServers(): List<Server>
    suspend fun saveVpnServers(servers: List<Server>)
    suspend fun getBlockedVpnServers(): List<Server>
    suspend fun blockVpnServers(vararg servers: Server)
    suspend fun unblockVpnServer(server: Server)
}
