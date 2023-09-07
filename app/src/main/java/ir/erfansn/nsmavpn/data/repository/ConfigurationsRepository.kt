package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.model.AppInfo
import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences
import ir.erfansn.nsmavpn.data.source.local.datastore.model.toConfigurations
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConfigurationsRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
) {
    val configurations = userPreferencesLocalDataSource
        .userPreferences
        .map(UserPreferences::toConfigurations)

    suspend fun setThemeMode(mode: Configurations.ThemeMode) {
        userPreferencesLocalDataSource.setThemeMode(mode)
    }

    suspend fun addAppToSplitTunnelingList(vararg apps: AppInfo) {
        userPreferencesLocalDataSource.addAppsToSplitTunnelingList(apps.toList())
    }

    suspend fun removeAppFromSplitTunnelingList(app: AppInfo) {
        userPreferencesLocalDataSource.removeAppFromSplitTunnelingList(app)
    }

    suspend fun removeAllAppsFromSplitTunnelingList() {
        userPreferencesLocalDataSource.clearSplitTunnelingList()
    }

    suspend fun setDynamicSchemeEnable(enable: Boolean) {
        userPreferencesLocalDataSource.setDynamicSchemeEnable(enable)
    }
}
