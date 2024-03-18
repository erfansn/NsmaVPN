package ir.erfansn.nsmavpn.data.source.local

import ir.erfansn.nsmavpn.data.source.local.datastore.Server

class MockVpnServersLocalDataSource : VpnServersLocalDataSource {

    private val allServers = mutableListOf<Server>()
    private val unavailableServers = mutableListOf<Server>()

    var deleteFromUnavailableVpnServersCallCount = 0
        private set

    override suspend fun getAvailableSstpVpnServers(): List<Server> {
        return allServers - unavailableServers.toSet()
    }

    override suspend fun getUnavailableVpnServers(): List<Server> {
        return unavailableServers
    }

    override suspend fun saveSstpVpnServers(servers: List<Server>) {
        allServers.addAll(servers)
    }

    override suspend fun saveAsUnavailableVpnServers(servers: List<Server>) {
        unavailableServers.addAll(servers)
    }

    override suspend fun deleteFromUnavailableVpnServers(servers: List<Server>) {
        unavailableServers.removeAll(servers)
        deleteFromUnavailableVpnServersCallCount++
    }
}
