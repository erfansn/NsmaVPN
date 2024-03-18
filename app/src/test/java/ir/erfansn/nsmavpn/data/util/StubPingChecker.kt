package ir.erfansn.nsmavpn.data.util

import ir.erfansn.nsmavpn.data.source.local.datastore.Server

class StubPingChecker : PingChecker {

    lateinit var measureResult: Map<Server, Double>

    override suspend fun measure(hostName: String, port: Int): Double {
        if (!::measureResult.isInitialized) return PingChecker.NOT_AVAILABLE
        return measureResult.filterKeys {
                with(it.address) {
                    this.hostName == hostName && this.portNumber == port
                }
            }
            .entries
            .singleOrNull()
            ?.value ?: PingChecker.NOT_AVAILABLE
    }
}
