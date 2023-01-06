package ir.erfansn.nsmavpn.ui.feature.signin

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.repository.ServersRepository
import ir.erfansn.nsmavpn.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val serversRepository: ServersRepository,
    private val userRepository: UserRepository,
    private val externalScope: CoroutineScope,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.None)
    val uiState = _uiState.asStateFlow()

    fun verifyVpnGateSubscription(userId: String) {
        viewModelScope.launch {
            _uiState.update {
                if (serversRepository.userIsSubscribedToVpnGateDailyMail(userId)) {
                    SignInUiState.Success(accountName = userId)
                } else {
                    SignInUiState.Error(
                        signOutIsNeed = true,
                        message = R.string.not_being_subscribed_to_vpngate
                    )
                }
            }
        }
    }

    fun saveUserEmailAddress(address: String) {
        externalScope.launch {
            userRepository.saveUserEmailAddress(address)
        }
    }
}

sealed interface SignInUiState {
    object None : SignInUiState
    data class Success(val accountName: String) : SignInUiState
    data class Error(
        val signOutIsNeed: Boolean = false,
        @StringRes val message: Int,
    ) : SignInUiState
}
