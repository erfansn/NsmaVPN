package ir.erfansn.nsmavpn.ui.feature.signin

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
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.SignedOut)
    val uiState = _uiState.asStateFlow()

    fun verifyVpnGateSubscription(id: String) {
        viewModelScope.launch {
            _uiState.update {
                if (serversRepository.userIsSubscribedToVpnGateDailyMail(id)) {
                    SignInUiState.SignIn(userAccountId = id)
                } else {
                    SignInUiState.Error(messageId = R.string.not_being_subscribed_to_vpngate)
                }
            }
        }
    }

    fun saveUserAccountId(id: String) {
        viewModelScope.launch {
            userRepository.saveUserAccountId(id = id)
        }
    }
}

sealed interface SignInUiState {
    object SignedOut : SignInUiState
    data class SignIn(val userAccountId: String) : SignInUiState
    data class Error(val messageId: Int) : SignInUiState
}
