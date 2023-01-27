package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.model.ThemeMode
import ir.erfansn.nsmavpn.data.source.local.datastore.ProfileProto
import ir.erfansn.nsmavpn.data.source.local.datastore.ThemeModeProto
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences
import javax.inject.Inject

class UserPreferencesLocalDataSource @Inject constructor(
    private val dataStore: DataStore<UserPreferences>,
) {
    val userPreferences = dataStore.data

    suspend fun saveUserEmailAddress(address: String) {
        dataStore.updateData {
            it.toBuilder().setEmailAddress(address).build()
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.updateData {
            it.toBuilder().setThemeMode(
                when (mode) {
                    ThemeMode.LIGHT -> ThemeModeProto.LIGHT
                    ThemeMode.DARK -> ThemeModeProto.DARK
                    ThemeMode.SYSTEM -> ThemeModeProto.SYSTEM
                }
            ).build()
        }
    }

    suspend fun saveUserProfile(profile: Profile) {
        dataStore.updateData {
            val profileProto = ProfileProto.newBuilder()
                .setEmailAddress(profile.emailAddress)
                .setAvatarUrl(profile.avatarUrl.orEmpty())
                .setDisplayName(profile.displayName)
                .build()
            it.toBuilder().setProfile(profileProto).build()
        }
    }
}
