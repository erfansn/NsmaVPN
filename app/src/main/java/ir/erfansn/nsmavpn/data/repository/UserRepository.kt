package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
) {
    suspend fun getUserAccountId(): String =
        userPreferencesLocalDataSource.userPreferencesStream.first().emailAddress

    suspend fun saveUserAccountId(id: String) {
        userPreferencesLocalDataSource.saveUserEmailAddress(address = id)
    }
}