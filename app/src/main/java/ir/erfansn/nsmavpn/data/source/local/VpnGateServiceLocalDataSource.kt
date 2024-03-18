package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnGateService
import ir.erfansn.nsmavpn.data.source.local.datastore.copy
import kotlinx.coroutines.flow.Flow
import java.net.URL
import javax.inject.Inject

interface VpnGateServiceLocalDataSource {
    val vpnGateService: Flow<VpnGateService>
    suspend fun saveMirrorSiteAddress(url: URL)
}

class DefaultVpnGateServiceLocalDataSource @Inject constructor(
    private val dataStore: DataStore<VpnGateService>
) : VpnGateServiceLocalDataSource {

    override val vpnGateService: Flow<VpnGateService> = dataStore.data

    override suspend fun saveMirrorSiteAddress(url: URL) {
        dataStore.updateData {
            it.copy {
                mirrorLink = url.toExternalForm()
            }
        }
    }
}
