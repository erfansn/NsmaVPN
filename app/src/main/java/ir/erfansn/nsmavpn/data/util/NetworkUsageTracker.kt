package ir.erfansn.nsmavpn.data.util

import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
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

class DefaultNetworkUsageTracker @Inject constructor(
    @ApplicationContext context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NetworkUsageTracker {

    init {
        require(context.isGrantedGetUsageStatsPermission)
    }

    private val networkStatsManager = context.getSystemService<NetworkStatsManager>()!!

    override fun trackUsage(startEpochTime: Long): Flow<NetworkUsage> =
        flow {
            var wifiStatsBucket = queryWifiStatsBucket(startEpochTime)
            var mobileStatsBucket = queryMobileStatsBucket(startEpochTime)

            while (true) {
                wifiStatsBucket = queryWifiStatsBucket(startEpochTime) ?: wifiStatsBucket
                mobileStatsBucket = queryMobileStatsBucket(startEpochTime) ?: mobileStatsBucket

                emit(
                    value = NetworkUsage(
                        received = wifiStatsBucket.rxBytes + mobileStatsBucket.rxBytes,
                        transmitted = wifiStatsBucket.txBytes + mobileStatsBucket.txBytes,
                    )
                )
                delay(3.seconds)
            }
        }.flowOn(ioDispatcher)

    private fun queryWifiStatsBucket(startEpochTime: Long) = networkStatsManager.querySummaryForDevice(
        ConnectivityManager.TYPE_WIFI,
        null,
        startEpochTime,
        Long.MAX_VALUE,
    )

    private fun queryMobileStatsBucket(startEpochTime: Long) = networkStatsManager.querySummaryForDevice(
        ConnectivityManager.TYPE_MOBILE,
        null,
        startEpochTime,
        Long.MAX_VALUE,
    )
}

interface NetworkUsageTracker {
    fun trackUsage(startEpochTime: Long): Flow<NetworkUsage>
}

val Context.isGrantedGetUsageStatsPermission: Boolean
    get() {
        val appOps = getSystemService<AppOpsManager>()!!
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        } else {
            appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        }
        return mode == MODE_ALLOWED
    }
