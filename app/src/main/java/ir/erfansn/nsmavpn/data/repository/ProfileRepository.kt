package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.model.isEmpty
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.model.toProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ProfileRepository {
    val userProfile: Flow<Profile>
    suspend fun saveUserProfile(avatarUrl: String, displayName: String, emailAddress: String)
    fun clearUserProfile()
    suspend fun isUserProfileSaved(): Boolean
}

class DefaultProfileRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
    private val applicationScope: CoroutineScope,
) : ProfileRepository {

    override val userProfile = userPreferencesLocalDataSource.userPreferences.map {
        it.toProfile()
    }

    override suspend fun saveUserProfile(
        avatarUrl: String,
        displayName: String,
        emailAddress: String,
    ) {
        val profile = Profile(
            avatarUrl = avatarUrl,
            displayName = displayName,
            emailAddress = emailAddress,
        )
        userPreferencesLocalDataSource.saveUserProfile(profile)
    }

    override fun clearUserProfile() {
        applicationScope.launch {
            userPreferencesLocalDataSource.clearUserProfile()
        }
    }

    override suspend fun isUserProfileSaved(): Boolean {
        return !userProfile.first().isEmpty()
    }
}
