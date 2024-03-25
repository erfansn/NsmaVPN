package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.source.local.datastore.UserProfile
import ir.erfansn.nsmavpn.data.source.local.datastore.copy
import javax.inject.Inject

interface UserProfileLocalDataSource {
    suspend fun saveUserProfile(profile: Profile)
    suspend fun clearUserProfile()
}

class DefaultUserProfileLocalDataSource @Inject constructor(
    private val dataStore: DataStore<UserProfile>
) : UserProfileLocalDataSource {

    override suspend fun saveUserProfile(profile: Profile) {
        dataStore.updateData {
            it.copy {
                avatarUrl = profile.avatarUrl
                emailAddress = profile.emailAddress
                displayName = profile.displayName
            }
        }
    }

    override suspend fun clearUserProfile() {
        dataStore.updateData {
            it.defaultInstanceForType
        }
    }
}
