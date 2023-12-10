package ir.erfansn.nsmavpn.data.util

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.erfansn.nsmavpn.data.model.AppInfo
import ir.erfansn.nsmavpn.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class DefaultInstalledAppsListProvider @Inject constructor(
    @ApplicationContext val context: Context,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
) : InstalledAppsListProvider {

    override val installedApps: Flow<List<AppInfo>> = flow {
        val packageManager = context.packageManager

        while (true) {
            emit(
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
                    }
            )
            delay(1.minutes)
        }
    }.flowOn(defaultDispatcher)
        .distinctUntilChanged()
}

interface InstalledAppsListProvider {
    val installedApps: Flow<List<AppInfo>>
}
