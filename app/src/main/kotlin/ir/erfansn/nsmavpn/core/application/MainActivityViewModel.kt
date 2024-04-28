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
package ir.erfansn.nsmavpn.core.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.core.NsmaVpnNotificationManager
import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.data.repository.ConfigurationsRepository
import ir.erfansn.nsmavpn.data.repository.UserProfileRepository
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnServiceAction
import ir.erfansn.nsmavpn.sync.VpnServersSyncManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    configurationsRepository: ConfigurationsRepository,
    private val userProfileRepository: UserProfileRepository,
    private val vpnServersSyncManager: VpnServersSyncManager,
    private val sstpVpnServiceAction: SstpVpnServiceAction,
    private val nsmaVpnNotificationManager: NsmaVpnNotificationManager
) : ViewModel() {

    val uiState = configurationsRepository.configurations.map {
        MainActivityUiState.Success(
            themeMode = it.themeMode,
            isEnableDynamicScheme = it.isEnableDynamicScheme,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainActivityUiState.Loading
    )

    fun resetApp() {
        sstpVpnServiceAction.disconnect(quietly = true)
        nsmaVpnNotificationManager.cancelErrorNotification()
        vpnServersSyncManager.endAllVpnServersSync()
        userProfileRepository.clearUserProfile()
    }
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(
        val themeMode: Configurations.ThemeMode,
        val isEnableDynamicScheme: Boolean,
    ) : MainActivityUiState
}
