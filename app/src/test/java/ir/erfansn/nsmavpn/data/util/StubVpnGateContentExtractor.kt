package ir.erfansn.nsmavpn.data.util

import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import java.net.URL

class StubVpnGateContentExtractor : VpnGateContentExtractor {

    lateinit var sstpServers: List<Server>
    override suspend fun extractSstpVpnServers(vpnGateAddress: URL): List<Server> {
        return sstpServers
    }
}
