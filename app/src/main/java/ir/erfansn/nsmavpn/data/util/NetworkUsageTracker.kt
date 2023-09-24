package ir.erfansn.nsmavpn.data.util

import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Build
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
            while (true) {
                val statsBucket = networkStatsManager.querySummaryForDevice(
                    ConnectivityManager.TYPE_VPN,
                    null,
                    startEpochTime,
                    Long.MAX_VALUE,
                )

                emit(
                    value = NetworkUsage(
                        received = statsBucket.rxBytes,
                        transmitted = statsBucket.txBytes,
                    )
                )
                delay(2.seconds)
            }
        }.flowOn(ioDispatcher)
}

interface NetworkUsageTracker {
    fun trackUsage(startEpochTime: Long): Flow<NetworkUsage>
}

val Context.isGrantedGetUsageStatsPermission: Boolean
    get() {
        val appOps = getSystemService<AppOpsManager>()!!
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(OPSTR_GET_USAGE_STATS, Binder.getCallingPid(), packageName)
        } else {
            appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Binder.getCallingPid(), packageName)
        }
        return mode == MODE_ALLOWED
    }
