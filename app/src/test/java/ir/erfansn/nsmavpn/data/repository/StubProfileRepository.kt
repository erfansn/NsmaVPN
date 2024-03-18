package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object StubProfileRepository : ProfileRepository {
    override val userProfile: Flow<Profile>
        get() = flowOf(
            Profile(
                avatarUrl = "",
                displayName = "",
                emailAddress = ""
            )
        )

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
