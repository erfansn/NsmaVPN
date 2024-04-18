package ir.erfansn.nsmavpn.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.sentry.Sentry
import ir.erfansn.nsmavpn.data.repository.DefaultVpnServersRepository
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

@HiltWorker
class VpnServersSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val serversRepository: DefaultVpnServersRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            serversRepository.collectVpnServers()
            reviseVpnServers()

            Result.success()
        }.getOrElse {
            if (it !is CancellationException) Sentry.captureException(it)
            Result.retry()
        }
    }

    private suspend fun reviseVpnServers() {
        serversRepository.unblockReachableVpnServers()
        serversRepository.blockUnreachableVpnServers()
    }

    companion object {
        const val SYNCHRONIZER_WORKER = "vpn_servers_sync"

        private val syncWorkerConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val WorkRequest =
            PeriodicWorkRequestBuilder<VpnServersSyncWorker>(
                repeatInterval = 12,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
            )
                .setConstraints(syncWorkerConstraints)
                .build()
    }
}
