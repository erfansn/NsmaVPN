package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.source.local.datastore.ThemeMode
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserPreferencesLocalDataSource @Inject constructor(
    private val dataStore: DataStore<UserPreferences>,
) {
    val userPreferencesStream = dataStore.data

    suspend fun saveUserAccountName(accountName: String) {
        dataStore.updateData {
            it.toBuilder().setAccountName(accountName).build()
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.updateData {
            it.toBuilder().setThemeMode(mode).build()
        }
    }

    suspend fun enableReconnection(enable: Boolean) {
        dataStore.updateData { userPrefs ->
            val reconnection = userPrefs.reconnection.toBuilder().setEnable(enable).build()
            userPrefs.toBuilder().setReconnection(reconnection).build()
        }
    }

    suspend fun setReconnectionIntervalTime(interval: Long) {
        dataStore.updateData { userPrefs ->
            val reconnection = userPrefs.reconnection.toBuilder().setIntervalTime(interval).build()
            userPrefs.toBuilder().setReconnection(reconnection).build()
        }
    }

    suspend fun setReconnectionRetryCount(count: Int) {
        dataStore.updateData { userPrefs ->
            val reconnection = userPrefs.reconnection.toBuilder().setRetryCount(count).build()
            userPrefs.toBuilder().setReconnection(reconnection).build()
        }
    }
}
