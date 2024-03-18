package ir.erfansn.nsmavpn.feature.settings.tunnelsplitting

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.data.model.AppInfo
import ir.erfansn.nsmavpn.data.repository.ConfigurationsRepository
import ir.erfansn.nsmavpn.data.util.InstalledAppsListProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TunnelSplittingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val configurationsRepository: ConfigurationsRepository,
    private val installedAppsListProvider: InstalledAppsListProvider,
) : ViewModel() {

    private val searchQuery = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")
    val uiState = combine(
        searchQuery,
        configurationsRepository.configurations,
    ) { searchQuery, configurations ->
        TunnelSplittingUiState(
            searchQuery = searchQuery,
            appItems = installedAppsListProvider.getCurrentInstalledApps().filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }.map {
                AppItemUiState(
                    appInfo = it,
                    allowed = it.id !in configurations.splitTunnelingAppIds
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = TunnelSplittingUiState(),
    )

    fun onSearchQueryChange(query: String) {
        savedStateHandle[KEY_SEARCH_QUERY] = query
    }

    fun toggleAllowedApp(appInfo: AppInfo, allow: Boolean) {
        viewModelScope.launch {
            if (allow) {
                configurationsRepository.removeAppFromSplitTunnelingList(appInfo)
            } else {
                configurationsRepository.addAppToSplitTunnelingList(appInfo)
            }
        }
    }

    fun changeAllAppsSplitTunnelingStatus(allow: Boolean) {
        viewModelScope.launch {
            if (allow) {
                configurationsRepository.removeAllAppsFromSplitTunnelingList()
            } else {
                val installedApps = installedAppsListProvider.getCurrentInstalledApps()
                configurationsRepository.addAppToSplitTunnelingList(*installedApps.toTypedArray())
            }
        }
    }

    companion object {
        private const val KEY_SEARCH_QUERY = "search_query"
    }
}

@Immutable
data class TunnelSplittingUiState(
    val searchQuery: String = "",
    val appItems: List<AppItemUiState>? = null
)

class AppItemUiState(
    val appInfo: AppInfo,
    val allowed: Boolean,
)
