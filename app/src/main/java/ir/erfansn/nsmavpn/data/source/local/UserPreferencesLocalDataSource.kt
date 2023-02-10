package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.model.ThemeMode
import ir.erfansn.nsmavpn.data.source.local.datastore.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Instead of using Impl suffix we use Default prefix because Impl suffix violates DRY principle.
 *
 * [Reference](https://link.medium.com/u5o2xd8Ahxb)
 */
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
                if (profile != null) {
                    profileProto = profileProto {
                        emailAddress = profile.emailAddress
                        avatarUrl = profile.avatarUrl.orEmpty()
                        displayName = profile.displayName
                    }
                } else {
                    clearProfileProto()
                }
            }
        }
    }
}

/**
 * Based on Dependency Rule in Clean Architecture inner circle nothing know about outer circle
 * Thus DataSource aware to Domain Models but
 * Repository not access to instantiation Dto Models
 */
interface UserPreferencesLocalDataSource {
    val userPreferences: Flow<UserPreferences>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun saveUserProfile(profile: Profile?)
}
