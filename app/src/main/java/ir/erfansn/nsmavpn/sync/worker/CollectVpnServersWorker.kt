package ir.erfansn.nsmavpn.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
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

        private const val COLLECTOR_TIME_INTERVAL = 12L
        private val collectorWorkerConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val WorkRequest =
            PeriodicWorkRequestBuilder<CollectVpnServersWorker>(
                repeatInterval = COLLECTOR_TIME_INTERVAL,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
            )
            .setConstraints(collectorWorkerConstraints)
            .build()
    }
}
