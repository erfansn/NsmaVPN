package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.model.ThemeMode
import ir.erfansn.nsmavpn.data.model.asThemeModeProto
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.model.asConfiguration
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConfigurationsRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
) {
    val configurations = userPreferencesLocalDataSource.userPreferences.map {
        it.asConfiguration()
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        userPreferencesLocalDataSource.setThemeMode(mode.asThemeModeProto())
    }
}
