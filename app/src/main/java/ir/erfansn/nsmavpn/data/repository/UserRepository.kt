package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.model.toProfile
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.remote.PersonInfoRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
    private val personInfoRemoteDataSource: PersonInfoRemoteDataSource,
    private val externalScope: CoroutineScope,
) {
    private val mutex = Mutex()
    private var userProfile: Profile? = null

    suspend fun getUserAccountId(): String =
        userPreferencesLocalDataSource.userPreferences.first().emailAddress

    suspend fun saveUserAccountId(userAccountId: String) {
        externalScope.launch {
            userPreferencesLocalDataSource.saveUserEmailAddress(userAccountId)
        }.join()
    }

    suspend fun getUserProfile(userAccountId: String): Profile {
        return externalScope.async {
            mutex.withLock {
                userProfile ?: personInfoRemoteDataSource.fetchPublicInfo(userAccountId).toProfile()
                    .also {
                        userProfile = it
                    }
            }
        }.await()
    }
}