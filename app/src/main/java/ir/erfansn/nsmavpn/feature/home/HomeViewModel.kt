package ir.erfansn.nsmavpn.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.core.AndroidString
import ir.erfansn.nsmavpn.data.repository.LastVpnConnectionRepository
import ir.erfansn.nsmavpn.data.repository.UserProfileRepository
import ir.erfansn.nsmavpn.data.util.NetworkUsageTracker
import ir.erfansn.nsmavpn.feature.home.vpn.ConnectionState
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnServiceAction
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnServiceState
import ir.erfansn.nsmavpn.sync.VpnServersSyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lastVpnConnectionRepository: LastVpnConnectionRepository,
    private val networkUsageTracker: NetworkUsageTracker,
    private val sstpVpnServiceAction: SstpVpnServiceAction,
    vpnServersSyncManager: VpnServersSyncManager,
    userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val vpnServiceState = MutableStateFlow(VpnServiceState())
    private val userError = MutableStateFlow<UserError?>(null)
    val uiState = combine(
        vpnServiceState,
        vpnServersSyncManager.isSyncing,
        userProfileRepository.userProfile,
        userError,
    ) { state, isSyncing, userProfile, userError ->
        HomeUiState(
            isSyncing = isSyncing,
            userAvatarUrl = userProfile.avatarUrl,
            vpnServiceState = state,
            userError = userError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val dataTraffic = vpnServiceState
        .combine(userError) { vpnService, error ->
            if (vpnService.state is ConnectionState.Connected) {
                val lastVpnConnection = lastVpnConnectionRepository.lastVpnConnection.first()

                networkUsageTracker.trackUsage(lastVpnConnection.epochTime).map { usage ->
                    DataTraffic(upload = usage.transmitted, download = usage.received)
                }
            } else {
                check(error !is UserError.UsageAccessPermissionLose)

                flowOf(
                    DataTraffic(download = 0, upload = 0)
                )
            }
        }
        .flattenConcat()
        .catch<DataTraffic?> { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DataTraffic(download = 0, upload = 0)
        )

    fun updateVpnServiceState(state: SstpVpnServiceState) {
        viewModelScope.launch {
            vpnServiceState.update {
                it.copy(
                    started = state.started,
                    state = state.connectionState
                )
            }
        }
    }

    fun connectToVpn() {
        clearUserError()

        if (!networkUsageTracker.isUsageAccessPermissionGrant) {
            userError.update { UserError.UsageAccessPermissionLose(AndroidString(R.string.usage_access_permission)) }
        }
        sstpVpnServiceAction.connect()
    }

    fun disconnectFromVpn() {
        sstpVpnServiceAction.disconnect()
    }

    fun notifyUserErrorShown() {
        clearUserError()
    }

    private fun clearUserError() {
        userError.update { null }
    }
}

data class HomeUiState(
    val userAvatarUrl: String? = null,
    val isSyncing: Boolean = false,
    val vpnServiceState: VpnServiceState = VpnServiceState(),
    val userError: UserError? = null
)

sealed interface UserError {
    data class UsageAccessPermissionLose(val message: AndroidString) : UserError
}

data class VpnServiceState(
    val started: Boolean = false,
    val state: ConnectionState = ConnectionState.Disconnected
)

data class DataTraffic(
    val upload: Long,
    val download: Long
)
