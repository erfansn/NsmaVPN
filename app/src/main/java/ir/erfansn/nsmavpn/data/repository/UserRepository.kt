package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
    private val externalScope: CoroutineScope,
) {
    suspend fun getUserAccountId(): String =
        userPreferencesLocalDataSource.userPreferences.first().emailAddress

    suspend fun saveUserAccountId(id: String) {
        externalScope.launch {
            userPreferencesLocalDataSource.saveUserEmailAddress(address = id)
        }.join()
    }
}