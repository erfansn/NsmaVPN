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
