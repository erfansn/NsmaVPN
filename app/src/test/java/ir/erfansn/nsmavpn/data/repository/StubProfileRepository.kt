package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.source.local.datastore.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object StubProfileRepository : ProfileRepository {
    override val userProfile: Flow<UserProfile>
        get() = flowOf(UserProfile.getDefaultInstance())

    override suspend fun saveUserProfile(
        avatarUrl: String,
        displayName: String,
        emailAddress: String,
    ) {
        TODO("Not yet implemented")
    }

    override fun clearUserProfile() {
        TODO("Not yet implemented")
    }

    override suspend fun isUserProfileSaved(): Boolean {
        TODO("Not yet implemented")
    }
}
