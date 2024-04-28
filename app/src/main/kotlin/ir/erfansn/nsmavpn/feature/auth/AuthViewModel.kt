/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.erfansn.nsmavpn.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.core.AndroidString
import ir.erfansn.nsmavpn.data.repository.UserProfileRepository
import ir.erfansn.nsmavpn.data.repository.VpnGateMailRepository
import ir.erfansn.nsmavpn.sync.VpnServersSyncManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val vpnGateMailRepository: VpnGateMailRepository,
    private val userProfileRepository: UserProfileRepository,
    private val vpnServersSyncManager: VpnServersSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private var verificationJob: Job? = null

    fun verifyVpnGateSubscription(account: GoogleSignInAccount) {
        verificationJob?.cancel()
        verificationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    subscriptionStatus = VpnGateSubscriptionStatus.Unknown,
                    errorMessage = null
                )
            }

            try {
                val isSubscribed = vpnGateMailRepository.isSubscribedToDailyMail(account.email!!)
                if (isSubscribed) {
                    userProfileRepository.saveUserProfile(
                        avatarUrl = account.photoUrl.toString().substringBeforeLast('='),
                        displayName = account.displayName.toString(),
                        emailAddress = account.email.toString()
                    )
                    vpnServersSyncManager.beginVpnServersSync()
                }

                _uiState.update {
                    it.copy(
                        subscriptionStatus = if (isSubscribed) VpnGateSubscriptionStatus.Is else VpnGateSubscriptionStatus.IsNot
                    )
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        errorMessage = AndroidString(R.string.network_problem)
                    )
                }
            }
        }
    }

    fun notifyMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class AuthUiState(
    val subscriptionStatus: VpnGateSubscriptionStatus = VpnGateSubscriptionStatus.Unknown,
    val errorMessage: AndroidString? = null,
)

enum class VpnGateSubscriptionStatus { Unknown, IsNot, Is }
