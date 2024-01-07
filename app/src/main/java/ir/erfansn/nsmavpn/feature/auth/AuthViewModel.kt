package ir.erfansn.nsmavpn.feature.auth

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.repository.ProfileRepository
import ir.erfansn.nsmavpn.data.repository.VpnGateMailRepository
import ir.erfansn.nsmavpn.sync.VpnServersSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val vpnGateMailRepository: VpnGateMailRepository,
    private val profileRepository: ProfileRepository,
    private val vpnServersSyncManager: VpnServersSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun verifyVpnGateSubscriptionAndSaveIt(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.update { it.copy(subscriptionStatus = VpnGateSubscriptionStatus.Unknown) }

            try {
                val isSubscribed = vpnGateMailRepository.isSubscribedToDailyMail(account.email!!)
                if (isSubscribed) {
                    saveUserProfile(account)
                    vpnServersSyncManager.beginVpnServersSyncTasks()
                }

                _uiState.update {
                    it.copy(
                        subscriptionStatus = if (isSubscribed) VpnGateSubscriptionStatus.Is else VpnGateSubscriptionStatus.IsNot
                    )
                }
            // Change to specific one
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = R.string.network_problem
                    )
                }
            }
        }
    }

    private suspend fun saveUserProfile(account: GoogleSignInAccount) {
        val profile = Profile(
            avatarUrl = account.photoUrl.toString().substringBeforeLast('='),
            displayName = account.displayName.toString(),
            emailAddress = account.email.toString()
        )
        Log.v(TAG, profile.toString())

        profileRepository.saveUserProfile(profile)
    }

    fun notifyMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}

@Immutable
data class AuthUiState(
    val subscriptionStatus: VpnGateSubscriptionStatus = VpnGateSubscriptionStatus.Unknown,
    @StringRes val errorMessage: Int? = null,
)

enum class VpnGateSubscriptionStatus { Unknown, IsNot, Is }
