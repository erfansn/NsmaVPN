package ir.erfansn.nsmavpn.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ir.erfansn.nsmavpn.data.repository.ServersRepository
import java.util.concurrent.TimeUnit

@HiltWorker
class CollectVpnServersWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val serversRepository: ServersRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            serversRepository.collectVpnServers()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val SERVER_COLLECTOR_WORKER = "server_collector"

        private val collectorWorkerConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val WorkRequest =
            PeriodicWorkRequestBuilder<CollectVpnServersWorker>(
                repeatInterval = 12,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
            )
            .setConstraints(collectorWorkerConstraints)
            .build()
    }
}
