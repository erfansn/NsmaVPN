package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import kotlinx.coroutines.CoroutineScope
import ir.erfansn.nsmavpn.data.source.local.datastore.model.asProfile
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
    private val externalScope: CoroutineScope,
) {
    val userProfile = userPreferencesLocalDataSource.userPreferences.map {
        it.asProfile()
    }

    suspend fun saveUserProfile(profile: Profile?) {
        externalScope.launch {
            userPreferencesLocalDataSource.saveUserProfile(profile)
        }.join()
    }
}