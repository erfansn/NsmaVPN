package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnServiceState
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnServiceStateKt
import ir.erfansn.nsmavpn.data.source.local.datastore.copy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultVpnServiceStateLocalDataSource @Inject constructor(
    private val dataStore: DataStore<VpnServiceState>
) : VpnServiceStateLocalDataSource {

    override val currentState: Flow<VpnServiceState> = dataStore.data

    override suspend fun updateState(block: VpnServiceStateKt.Dsl.() -> Unit) {
        dataStore.updateData {
            it.copy(block)
        }
    }
}

interface VpnServiceStateLocalDataSource {
    val currentState: Flow<VpnServiceState>
    suspend fun updateState(block: VpnServiceStateKt.Dsl.() -> Unit)
}
