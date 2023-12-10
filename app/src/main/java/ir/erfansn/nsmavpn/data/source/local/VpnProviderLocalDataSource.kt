package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.model.MirrorLink
import ir.erfansn.nsmavpn.data.source.local.datastore.Protocol
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.UrlParts
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnProvider
import ir.erfansn.nsmavpn.data.source.local.datastore.copy
import ir.erfansn.nsmavpn.data.source.local.datastore.urlParts
import ir.erfansn.nsmavpn.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Instead of using Impl suffix we use Default prefix because Impl suffix violates DRY principle.
 *
 * [Reference](https://link.medium.com/u5o2xd8Ahxb)
 */
class DefaultVpnProviderLocalDataSource @Inject constructor(
    private val dataStore: DataStore<VpnProvider>,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : VpnProviderLocalDataSource {

    override suspend fun getVpnProviderAddress(): UrlParts =
        dataStore.data.first().address

    override suspend fun saveVpnProviderAddress(mirrorLink: MirrorLink) {
        dataStore.updateData {
            it.copy {
                address = urlParts {
                    protocol = if (mirrorLink.protocol == "http") Protocol.HTTP else Protocol.HTTPS
                    hostName = mirrorLink.hostName
                    portNumber = mirrorLink.port.toInt()
                }
            }
        }
    }

    override suspend fun getAvailableVpnServers(): List<Server> = withContext(defaultDispatcher) {
        dataStore.data.first().let { vpnProvider ->
            vpnProvider.serversList.filter { it !in vpnProvider.blacklistedServersList }
        }
    }

    override suspend fun saveVpnServers(servers: List<Server>) {
        dataStore.updateData { vpnProvider ->
            vpnProvider.copy {
                val newServers = servers.filter { it !in this.servers }
                this.servers.addAll(newServers)
            }
        }
    }

    override suspend fun getBlockedVpnServers(): List<Server> {
        return dataStore.data.first().blacklistedServersList
    }

    override suspend fun blockVpnServers(vararg servers: Server) {
        dataStore.updateData { vpnProvider ->
            vpnProvider.copy {
                val newServers = servers.filter { it !in blacklistedServers }
                blacklistedServers.addAll(newServers)
            }
        }
    }

    override suspend fun unblockVpnServers(servers: List<Server>) {
        dataStore.updateData { vpnProvider ->
            vpnProvider.copy {
                blacklistedServers.filter {
                    it !in servers
                }.also {
                    blacklistedServers.clear()
                }.let {
                    blacklistedServers.addAll(it)
                }
            }
        }
    }
}

interface VpnProviderLocalDataSource {
    suspend fun getVpnProviderAddress(): UrlParts
    suspend fun saveVpnProviderAddress(mirrorLink: MirrorLink)
    suspend fun getAvailableVpnServers(): List<Server>
    suspend fun saveVpnServers(servers: List<Server>)
    suspend fun getBlockedVpnServers(): List<Server>
    suspend fun blockVpnServers(vararg servers: Server)
    suspend fun unblockVpnServers(servers: List<Server>)
}
