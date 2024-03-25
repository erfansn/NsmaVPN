package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.source.local.datastore.UserProfile
import ir.erfansn.nsmavpn.data.source.local.datastore.copy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserProfileLocalDataSource {
    val userProfile: Flow<UserProfile>
    suspend fun saveUserProfile(
        avatarUrl: String,
        displayName: String,
        emailAddress: String
    )
    suspend fun clearUserProfile()
}

class DefaultUserProfileLocalDataSource @Inject constructor(
    private val dataStore: DataStore<UserProfile>
) : UserProfileLocalDataSource {

    override val userProfile: Flow<UserProfile> = dataStore.data

    override suspend fun saveUserProfile(
        avatarUrl: String,
        displayName: String,
        emailAddress: String,
    ) {
        dataStore.updateData {
            it.copy {
                this.avatarUrl = avatarUrl
                this.emailAddress = emailAddress
                this.displayName = displayName
            }
        }
    }

    override suspend fun clearUserProfile() {
        dataStore.updateData {
            it.defaultInstanceForType
        }
    }
}
