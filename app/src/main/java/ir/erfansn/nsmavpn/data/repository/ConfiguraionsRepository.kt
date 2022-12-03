package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.model.toConfiguration
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.ThemeMode
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConfigurationsRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
) {
    fun getConfigurationStream() = userPreferencesLocalDataSource.userPreferencesStream.map {
        it.toConfiguration()
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        userPreferencesLocalDataSource.setThemeMode(mode)
    }

    suspend fun enableReconnectionMode(enable: Boolean) {
        userPreferencesLocalDataSource.enableReconnection(enable)
    }

    suspend fun setReconnectionIntervalTime(interval: Long) {
        userPreferencesLocalDataSource.setReconnectionIntervalTime(interval)
    }

    suspend fun setReconnectionRetryCount(count: Int) {
        userPreferencesLocalDataSource.setReconnectionRetryCount(count)
    }
}