package ir.erfansn.nsmavpn.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object StubVpnServersSyncManager : VpnServersSyncManager {
    override val isSyncing: Flow<Boolean>
        get() = flowOf(true)

    override fun beginVpnServersSync() {
        TODO("Not yet implemented")
    }

    override fun endAllVpnServersSync() {
        TODO("Not yet implemented")
    }
}