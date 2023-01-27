package ir.erfansn.nsmavpn.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.model.toProfile
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.remote.PersonInfoRemoteDataSource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
    private val personInfoRemoteDataSource: PersonInfoRemoteDataSource,
    private val externalScope: CoroutineScope,
) {
    val userProfile = userPreferencesLocalDataSource.userPreferences.map {
        Profile(
            avatarUrl = it.profile.avatarUrl.ifEmpty { null },
            displayName = it.profile.displayName,
            emailAddress = it.profile.emailAddress
        )
    }

    private val mutex = Mutex()
    private var _userProfile: Profile? = null

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
                _userProfile ?: personInfoRemoteDataSource.fetchPublicInfo(userAccountId).toProfile()
                    .also {
                        _userProfile = it
                    }
            }
        }.await()
    }

    suspend fun saveUserProfile(profile: Profile) {
        externalScope.launch {
            userPreferencesLocalDataSource.saveUserProfile(profile)
        }.join()
    }
}