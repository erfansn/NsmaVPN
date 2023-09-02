package ir.erfansn.nsmavpn.feature.settings.tunnelsplitting

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class TunnelSplittingViewModel : ViewModel() {

    lateinit var uiState: StateFlow<TunnelSplittingUiState>

    fun updateFilter(s: String) {
        TODO("Not yet implemented")
    }

    fun toggleAllowedApp(b: Boolean) {
        TODO("Not yet implemented")
    }

    fun changeAllAppsTrafficUsingStatus(b: Boolean) {
        TODO("Not yet implemented")
    }
}

@Immutable
data class TunnelSplittingUiState(
    val filterText: String = "",
    val appItems: List<AppItemUiState> = emptyList()
)

data class AppItemUiState(
    val appInfo: AppInfo,
    val allowed: Boolean,
)

data class AppInfo(
    val id: String,
    val name: String,
    val icon: Drawable,
)
