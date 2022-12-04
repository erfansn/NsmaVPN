package ir.erfansn.nsmavpn.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ir.erfansn.nsmavpn.data.repository.ServersRepository
import ir.erfansn.nsmavpn.data.repository.UserRepository
import java.util.concurrent.TimeUnit

@HiltWorker
class ServerCollectorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val serversRepository: ServersRepository,
    private val userRepository: UserRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = userRepository.getUserEmailAddress()
        return try {
            serversRepository.collectVpnServers(userId)
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

        val workRequest = PeriodicWorkRequestBuilder<ServerCollectorWorker>(
            repeatInterval = COLLECTOR_TIME_INTERVAL,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setConstraints(collectorWorkerConstraints)
            .build()
    }
}
