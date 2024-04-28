/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
