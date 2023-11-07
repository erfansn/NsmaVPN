package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.source.local.VpnServiceStateLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnServiceState
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnServiceStateKt
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultVpnServiceStateRepository @Inject constructor(
    private val vpnServiceStateLocalDataSource: VpnServiceStateLocalDataSource
) : VpnServiceStateRepository {

    override val currentState: Flow<VpnServiceState> = vpnServiceStateLocalDataSource.currentState

    override suspend fun updateState(block: VpnServiceStateKt.Dsl.() -> Unit) {
        vpnServiceStateLocalDataSource.updateState(block)
    }
}

interface VpnServiceStateRepository {
    val currentState: Flow<VpnServiceState>
    suspend fun updateState(block: VpnServiceStateKt.Dsl.() -> Unit)
}