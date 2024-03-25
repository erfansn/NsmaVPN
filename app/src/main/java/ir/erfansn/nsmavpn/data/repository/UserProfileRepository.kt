package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.model.isEmpty
import ir.erfansn.nsmavpn.data.source.local.UserProfileLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

interface UserProfileRepository {
    val userProfile: Flow<UserProfile>
    suspend fun saveUserProfile(avatarUrl: String, displayName: String, emailAddress: String)
    fun clearUserProfile()
    suspend fun isUserProfileSaved(): Boolean
}

class DefaultUserProfileRepository @Inject constructor(
    private val userProfileLocalDataSource: UserProfileLocalDataSource,
    private val applicationScope: CoroutineScope,
) : UserProfileRepository {

    override val userProfile: Flow<UserProfile> = userProfileLocalDataSource.userProfile

    override suspend fun saveUserProfile(
        avatarUrl: String,
        displayName: String,
        emailAddress: String,
    ) {
        userProfileLocalDataSource.saveUserProfile(avatarUrl, displayName, emailAddress)
    }

    override fun clearUserProfile() {
        applicationScope.launch {
            userProfileLocalDataSource.clearUserProfile()
        }
    }

    override suspend fun isUserProfileSaved(): Boolean {
        return !userProfile.first().isEmpty()
    }
}
