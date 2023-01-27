package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.ProfileProto
import ir.erfansn.nsmavpn.data.source.remote.PersonInfoRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
            val profileProto = ProfileProto.newBuilder()
                .setEmailAddress(profile.emailAddress)
                .setAvatarUrl(profile.avatarUrl.orEmpty())
                .setDisplayName(profile.displayName)
                .build()

            userPreferencesLocalDataSource.saveUserProfile(profileProto)
        }.join()
    }
}