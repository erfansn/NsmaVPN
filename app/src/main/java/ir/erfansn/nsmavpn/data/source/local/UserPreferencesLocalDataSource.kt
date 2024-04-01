package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.model.AppInfo
import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.data.source.local.datastore.ThemeModeProto
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences
import ir.erfansn.nsmavpn.data.source.local.datastore.copy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserPreferencesLocalDataSource {
    val userPreferences: Flow<UserPreferences>
    suspend fun setThemeMode(mode: Configurations.ThemeMode)
    suspend fun setDynamicSchemeEnable(enable: Boolean)
    suspend fun addAppsToSplitTunnelingList(apps: List<AppInfo>)
    suspend fun removeAppFromSplitTunnelingList(app: AppInfo)
    suspend fun clearSplitTunnelingList()
}

class DefaultUserPreferencesLocalDataSource @Inject constructor(
    private val dataStore: DataStore<UserPreferences>,
) : UserPreferencesLocalDataSource {

    override val userPreferences = dataStore.data

    override suspend fun setThemeMode(mode: Configurations.ThemeMode) {
        dataStore.updateData {
            it.copy {
                themeModeProto = when (mode) {
                    Configurations.ThemeMode.Light -> ThemeModeProto.LIGHT
                    Configurations.ThemeMode.Dark -> ThemeModeProto.DARK
                    Configurations.ThemeMode.System -> ThemeModeProto.SYSTEM
                }
            }
        }
    }

    override suspend fun setDynamicSchemeEnable(enable: Boolean) {
        dataStore.updateData {
            it.copy {
                enableDynamicScheme = enable
            }
        }
    }

    override suspend fun addAppsToSplitTunnelingList(apps: List<AppInfo>) {
        dataStore.updateData {
            it.copy {
                splitTunnelingAppId.addAll(apps.map(AppInfo::id))
            }
        }
    }

    override suspend fun removeAppFromSplitTunnelingList(app: AppInfo) {
        dataStore.updateData {
            it.copy {
                with(splitTunnelingAppId) {
                    val appsId = filter { id -> id != app.id }
                    clear()
                    addAll(appsId)
                }
            }
        }
    }

    override suspend fun clearSplitTunnelingList() {
        dataStore.updateData {
            it.copy {
                splitTunnelingAppId.clear()
            }
        }
    }
}
