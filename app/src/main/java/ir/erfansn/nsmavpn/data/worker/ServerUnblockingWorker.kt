package ir.erfansn.nsmavpn.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ir.erfansn.nsmavpn.data.repository.ServersRepository
import java.util.concurrent.TimeUnit

@HiltWorker
class ServerUnblockingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val serversRepository: ServersRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            serversRepository.unblockAvailableVpnServerFromBlacklistRandomly()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val SERVER_UNBLOCKING_WORKER = "server_blocking"

        private const val UNBLOCKING_TIME_INTERVAL = 24L
        private val unblockingWorkerConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ServerUnblockingWorker>(
            repeatInterval = UNBLOCKING_TIME_INTERVAL,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        )
            .setConstraints(unblockingWorkerConstraints)
            .build()
    }
}
