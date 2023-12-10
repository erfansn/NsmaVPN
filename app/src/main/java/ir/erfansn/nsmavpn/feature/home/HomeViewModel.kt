package ir.erfansn.nsmavpn.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.data.repository.LastVpnConnectionRepository
import ir.erfansn.nsmavpn.data.repository.ProfileRepository
import ir.erfansn.nsmavpn.data.util.NetworkUsageTracker
import ir.erfansn.nsmavpn.feature.home.vpn.ConnectionState
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnServiceState
import ir.erfansn.nsmavpn.sync.VpnServersSyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lastVpnConnectionRepository: LastVpnConnectionRepository,
    private val networkUsageTracker: NetworkUsageTracker,
    vpnServersSyncManager: VpnServersSyncManager,
    profileRepository: ProfileRepository,
) : ViewModel() {

    private val vpnServiceState = MutableStateFlow(VpnServiceState())
    val uiState = combine(
        vpnServiceState,
        vpnServersSyncManager.isSyncing,
        profileRepository.userProfile,
    ) { started, isSyncing, userProfile ->
        HomeUiState(
            isSyncing = isSyncing,
            userAvatarUrl = userProfile.avatarUrl,
            vpnServiceState = started,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val dataTraffic = uiState
        .flatMapLatest {
            check(networkUsageTracker.hasPermission)

            if (it.vpnServiceState.state is ConnectionState.Connected) {
                val lastVpnConnection = lastVpnConnectionRepository.lastVpnConnection.first()

                networkUsageTracker.trackUsage(lastVpnConnection.epochTime).map { usage ->
                    DataTraffic(upload = usage.transmitted, download = usage.received)
                }
            } else {
                flowOf(
                    DataTraffic(download = 0, upload = 0)
                )
            }
        }
        .catch<DataTraffic?> { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DataTraffic(download = 0, upload = 0)
        )

    fun updateVpnServiceState(state: SstpVpnServiceState) {
        vpnServiceState.update { it.copy(started = state.started, state = state.connectionState) }
    }
}

data class HomeUiState(
    val userAvatarUrl: String = "",
    val isSyncing: Boolean = false,
    val vpnServiceState: VpnServiceState = VpnServiceState(),
)

data class VpnServiceState(
    val started: Boolean = false,
    val state: ConnectionState = ConnectionState.Disconnected
)

data class DataTraffic(
    val upload: Long,
    val download: Long
)
