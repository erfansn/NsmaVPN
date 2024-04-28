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

import ir.erfansn.nsmavpn.data.model.AppInfo
import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences
import ir.erfansn.nsmavpn.data.source.local.datastore.model.toConfigurations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ConfigurationsRepository {
    val configurations: Flow<Configurations>
    suspend fun setThemeMode(mode: Configurations.ThemeMode)
    suspend fun addAppToSplitTunnelingList(vararg apps: AppInfo)
    suspend fun removeAppFromSplitTunnelingList(app: AppInfo)
    suspend fun removeAllAppsFromSplitTunnelingList()
    suspend fun setDynamicSchemeEnable(enable: Boolean)
}

class DefaultConfigurationsRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
) : ConfigurationsRepository {

    override val configurations = userPreferencesLocalDataSource
        .userPreferences
        .map(UserPreferences::toConfigurations)

    override suspend fun setThemeMode(mode: Configurations.ThemeMode) {
        userPreferencesLocalDataSource.setThemeMode(mode)
    }

    override suspend fun addAppToSplitTunnelingList(vararg apps: AppInfo) {
        userPreferencesLocalDataSource.addAppsToSplitTunnelingList(apps.toList())
    }

    override suspend fun removeAppFromSplitTunnelingList(app: AppInfo) {
        userPreferencesLocalDataSource.removeAppFromSplitTunnelingList(app)
    }

    override suspend fun removeAllAppsFromSplitTunnelingList() {
        userPreferencesLocalDataSource.clearSplitTunnelingList()
    }

    override suspend fun setDynamicSchemeEnable(enable: Boolean) {
        userPreferencesLocalDataSource.setDynamicSchemeEnable(enable)
    }
}
