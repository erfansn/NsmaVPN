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
import ir.erfansn.nsmavpn.data.repository.ServersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val serversRepository: ServersRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            serversRepository.stopVpnServersWorker()
            // Clear profile in any conditions
            profileRepository.saveUserProfile(null)
        }
    }

    fun verifyVpnGateSubscriptionAndSaveIt(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.update { it.copy(subscriptionStatus = VpnGateSubscriptionStatus.Unknown) }

            try {
                val isSubscribed = serversRepository.isSubscribedToVpnGateDailyMail(account.email!!)
                if (isSubscribed) {
                    saveUserProfile(account)
                }

                _uiState.update {
                    it.copy(
                        subscriptionStatus = if (isSubscribed) VpnGateSubscriptionStatus.Is else VpnGateSubscriptionStatus.Not
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

enum class VpnGateSubscriptionStatus { Unknown, Is, Not }
