package ir.erfansn.nsmavpn.feature.settings

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.data.model.ThemeMode
import ir.erfansn.nsmavpn.data.repository.ConfigurationsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val configurationsRepository: ConfigurationsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = configurationsRepository.configurations.map {
        SettingsUiState(themeMode = it.themeMode)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            configurationsRepository.setThemeMode(themeMode)
        }
    }
}

@Immutable
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)
