package ir.erfansn.nsmavpn.data.util

import android.app.AppOpsManager
import android.app.usage.NetworkStats.Bucket
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.erfansn.nsmavpn.data.model.NetworkUsage
import ir.erfansn.nsmavpn.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

interface NetworkUsageTracker {
    val isUsageAccessPermissionGrant: Boolean
    fun trackUsage(startEpochTime: Long): Flow<NetworkUsage>
}

class DefaultNetworkUsageTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NetworkUsageTracker {

    private val networkStatsManager = context.getSystemService<NetworkStatsManager>()

    override val isUsageAccessPermissionGrant: Boolean
        get() {
            val appOps = context.getSystemService<AppOpsManager>() ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            } else {
                appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            }
            return mode == AppOpsManager.MODE_ALLOWED
        }

    override fun trackUsage(startEpochTime: Long): Flow<NetworkUsage> =
        flow {
            while (true) {
                val wifiStatsBucket = queryWifiStatsBucket(startEpochTime)
                val mobileStatsBucket = queryMobileStatsBucket(startEpochTime)

                emit(
                    value = NetworkUsage(
                        received = wifiStatsBucket.rxBytes + mobileStatsBucket.rxBytes,
                        transmitted = wifiStatsBucket.txBytes + mobileStatsBucket.txBytes,
                    )
                )
                delay(1.seconds)
            }
        }.flowOn(ioDispatcher)

    private fun queryWifiStatsBucket(startEpochTime: Long) = queryStatsBucket(
        networkType = ConnectivityManager.TYPE_WIFI,
        startEpochTime = startEpochTime,
    )

    private fun queryMobileStatsBucket(startEpochTime: Long) = queryStatsBucket(
        networkType = ConnectivityManager.TYPE_MOBILE,
        startEpochTime = startEpochTime,
    )

    private fun queryStatsBucket(
        networkType: Int,
        startEpochTime: Long,
    ): Bucket {
        checkNotNull(networkStatsManager) { "Cannot access to NetworkStatsManager!" }

        return networkStatsManager.querySummaryForDevice(
            networkType,
            null,
            startEpochTime,
            Long.MAX_VALUE,
        ) ?: run {
            error("GetUsageAccess permission doesn't permitted or internal error occurred")
        }
    }
}
