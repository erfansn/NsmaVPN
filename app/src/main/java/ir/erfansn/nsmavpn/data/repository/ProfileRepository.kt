package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.model.toProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
    private val applicationScope: CoroutineScope,
) {
    val userProfile = userPreferencesLocalDataSource.userPreferences.map {
        it.toProfile()
    }

    suspend fun saveUserProfile(profile: Profile) {
        userPreferencesLocalDataSource.saveUserProfile(profile)
    }

    fun clearUserProfile() {
        applicationScope.launch {
            userPreferencesLocalDataSource.clearUserProfile()
        }
    }
}
