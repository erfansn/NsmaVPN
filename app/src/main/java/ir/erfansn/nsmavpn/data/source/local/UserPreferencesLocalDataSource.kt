package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
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

    suspend fun setThemeMode(mode: ThemeModeProto) {
        dataStore.updateData {
            it.toBuilder().setThemeModeProto(mode).build()
        }
    }

    suspend fun saveUserProfile(profile: ProfileProto) {
        dataStore.updateData {
            it.toBuilder().setProfileProto(profile).build()
        }
    }
}
