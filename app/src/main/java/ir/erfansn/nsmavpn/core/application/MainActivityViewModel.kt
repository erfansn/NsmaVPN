package ir.erfansn.nsmavpn.core.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.data.model.isEmpty
import ir.erfansn.nsmavpn.data.repository.ConfigurationsRepository
import ir.erfansn.nsmavpn.data.repository.ProfileRepository
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnServiceAction
import ir.erfansn.nsmavpn.sync.VpnServersSyncManager
import ir.erfansn.nsmavpn.core.NsmaVpnNotificationManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    configurationsRepository: ConfigurationsRepository,
    private val profileRepository: ProfileRepository,
    private val vpnServersSyncManager: VpnServersSyncManager,
    private val sstpVpnServiceAction: SstpVpnServiceAction,
    private val nsmaVpnNotificationManager: NsmaVpnNotificationManager
) : ViewModel() {

    val uiState = configurationsRepository.configurations.combine(profileRepository.userProfile) { configurations, userProfile ->
        MainActivityUiState.Success(
            themeMode = configurations.themeMode,
            isEnableDynamicScheme = configurations.isEnableDynamicScheme,
            isCompletedAuthFlow = userProfile.isEmpty()
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
        profileRepository.clearUserProfile()
    }
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(
        val themeMode: Configurations.ThemeMode,
        val isEnableDynamicScheme: Boolean,
        val isCompletedAuthFlow: Boolean
    ) : MainActivityUiState
}
