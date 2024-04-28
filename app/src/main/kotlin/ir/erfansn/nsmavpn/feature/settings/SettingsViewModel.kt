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
class SettingsViewModel @Inject constructor(
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
