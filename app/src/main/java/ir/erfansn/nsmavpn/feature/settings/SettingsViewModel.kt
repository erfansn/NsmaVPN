package ir.erfansn.nsmavpn.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.data.repository.ConfigurationsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val configurationsRepository: ConfigurationsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = configurationsRepository
        .configurations
        .map {
            SettingsUiState(
                themeMode = it.themeMode,
                isEnableDynamicScheme = it.isEnableDynamicScheme,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun updateThemeMode(themeMode: Configurations.ThemeMode) {
        viewModelScope.launch {
            configurationsRepository.setThemeMode(themeMode)
        }
    }

    fun toggleDynamicScheme() {
        viewModelScope.launch {
            configurationsRepository.setDynamicSchemeEnable(!uiState.value.isEnableDynamicScheme)
        }
    }
}

data class SettingsUiState(
    val themeMode: Configurations.ThemeMode = Configurations.ThemeMode.System,
    val isEnableDynamicScheme: Boolean = false,
)
