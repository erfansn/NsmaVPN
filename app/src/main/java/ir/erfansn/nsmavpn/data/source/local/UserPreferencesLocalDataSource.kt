package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.model.ThemeMode
import ir.erfansn.nsmavpn.data.source.local.datastore.ThemeModeProto
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences
import javax.inject.Inject

class UserPreferencesLocalDataSource @Inject constructor(
    private val dataStore: DataStore<UserPreferences>,
) {
    val userPreferencesStream = dataStore.data

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
}
