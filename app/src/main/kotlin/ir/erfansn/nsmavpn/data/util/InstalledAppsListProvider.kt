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
package ir.erfansn.nsmavpn.data.util

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.erfansn.nsmavpn.data.model.AppInfo
import ir.erfansn.nsmavpn.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface InstalledAppsListProvider {
    suspend fun getCurrentInstalledApps(): List<AppInfo>
}

class DefaultInstalledAppsListProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : InstalledAppsListProvider {

    private lateinit var installedApps: List<AppInfo>

    override suspend fun getCurrentInstalledApps(): List<AppInfo> {
        if (::installedApps.isInitialized) return installedApps

        val packageManager = context.packageManager
        return withContext(defaultDispatcher) {
            packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
                .map {
                    it.applicationInfo
                }.sortedBy {
                    it.loadLabel(packageManager).toString()
                }.filter {
                    it.packageName != context.packageName
                }.map {
                    AppInfo(
                        id = it.packageName,
                        name = it.loadLabel(packageManager).toString(),
                        icon = it.loadIcon(packageManager),
                    )
                }.also {
                    installedApps = it
                }
        }
    }
}
