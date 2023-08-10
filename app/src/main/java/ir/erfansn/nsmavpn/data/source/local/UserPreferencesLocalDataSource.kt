package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.model.ThemeMode
import ir.erfansn.nsmavpn.data.source.local.datastore.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultUserPreferencesLocalDataSource @Inject constructor(
    private val dataStore: DataStore<UserPreferences>,
) : UserPreferencesLocalDataSource {

    override val userPreferences = dataStore.data

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.updateData {
            it.copy {
                themeModeProto = when (mode) {
                    ThemeMode.LIGHT -> ThemeModeProto.LIGHT
                    ThemeMode.DARK -> ThemeModeProto.DARK
                    ThemeMode.SYSTEM -> ThemeModeProto.SYSTEM
                }
            }
        }
    }

    override suspend fun saveUserProfile(profile: Profile?) {
        dataStore.updateData {
            it.copy {
                if (profile == null) {
                    clearProfileProto()
                    return@copy
                }

                profileProto = profileProto {
                    avatarUrl = profile.avatarUrl
                    emailAddress = profile.emailAddress
                    displayName = profile.displayName
                }
            }
        }
    }
}

interface UserPreferencesLocalDataSource {
    val userPreferences: Flow<UserPreferences>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun saveUserProfile(profile: Profile?)
}
