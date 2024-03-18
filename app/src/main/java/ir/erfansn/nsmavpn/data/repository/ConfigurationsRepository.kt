package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.model.AppInfo
import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences
import ir.erfansn.nsmavpn.data.source.local.datastore.model.toConfigurations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ConfigurationsRepository {
    val configurations: Flow<Configurations>
    suspend fun setThemeMode(mode: Configurations.ThemeMode)
    suspend fun addAppToSplitTunnelingList(vararg apps: AppInfo)
    suspend fun removeAppFromSplitTunnelingList(app: AppInfo)
    suspend fun removeAllAppsFromSplitTunnelingList()
    suspend fun setDynamicSchemeEnable(enable: Boolean)
}

class DefaultConfigurationsRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
) : ConfigurationsRepository {

    override val configurations = userPreferencesLocalDataSource
        .userPreferences
        .map(UserPreferences::toConfigurations)

    override suspend fun setThemeMode(mode: Configurations.ThemeMode) {
        userPreferencesLocalDataSource.setThemeMode(mode)
    }

    override suspend fun addAppToSplitTunnelingList(vararg apps: AppInfo) {
        userPreferencesLocalDataSource.addAppsToSplitTunnelingList(apps.toList())
    }

    override suspend fun removeAppFromSplitTunnelingList(app: AppInfo) {
        userPreferencesLocalDataSource.removeAppFromSplitTunnelingList(app)
    }

    override suspend fun removeAllAppsFromSplitTunnelingList() {
        userPreferencesLocalDataSource.clearSplitTunnelingList()
    }

    override suspend fun setDynamicSchemeEnable(enable: Boolean) {
        userPreferencesLocalDataSource.setDynamicSchemeEnable(enable)
    }
}
