package ir.erfansn.nsmavpn.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object StubNetworkMonitor : NetworkMonitor {
    override val isOnline: Flow<Boolean>
        get() = flowOf(true)
}