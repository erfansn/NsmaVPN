package ir.erfansn.nsmavpn.data.util

import ir.erfansn.nsmavpn.data.model.NetworkUsage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object FakeNetworkUsageTracker : NetworkUsageTracker {

    override fun trackUsage(startEpochTime: Long): Flow<NetworkUsage> {
        require(startEpochTime >= 0)
        return flowOf(
            NetworkUsage(
                received = 10,
                transmitted = 10,
            )
        )
    }
}