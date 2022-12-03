package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
) {
    suspend fun getUserEmailAddress(): String =
        userPreferencesLocalDataSource.userPreferencesStream.first().accountName

    suspend fun saveUserEmailAddress(email: String) {
        userPreferencesLocalDataSource.saveUserAccountName(email)
    }
}