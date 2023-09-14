package ir.erfansn.nsmavpn.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.data.repository.ProfileRepository
import ir.erfansn.nsmavpn.data.repository.ServersRepository
import ir.erfansn.nsmavpn.data.repository.VpnGateMailRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val serversRepository: ServersRepository,
    private val vpnGateMailRepository: VpnGateMailRepository,
) : ViewModel() {

    val uiState = profileRepository
        .userProfile
        .map {
            val isSubscribedToVpnGate = vpnGateMailRepository.isSubscribedToDailyMail(it.emailAddress)

            ProfileUiState(
                avatarUrl = it.avatarUrl,
                emailAddress = it.emailAddress,
                displayName = it.displayName,
                vpnGateSubscriptionStatus = VpnGateSubscriptionStatus(isSubscribedToVpnGate)
            )
        }
        .retry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState()
        )

    fun signOutFromAccount() {
        serversRepository.stopVpnServersWorker()
        profileRepository.clearUserProfile()
    }
}

data class ProfileUiState(
    val avatarUrl: String? = null,
    val emailAddress: String = "",
    val displayName: String = "",
    val vpnGateSubscriptionStatus: VpnGateSubscriptionStatus? = null,
) {
    val isInfoLoaded: Boolean get() = emailAddress.isNotEmpty() && displayName.isNotEmpty()
}

@JvmInline
value class VpnGateSubscriptionStatus(val isSubscribed: Boolean)
