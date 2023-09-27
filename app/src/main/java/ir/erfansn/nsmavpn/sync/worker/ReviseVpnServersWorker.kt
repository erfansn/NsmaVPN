package ir.erfansn.nsmavpn.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ir.erfansn.nsmavpn.data.repository.ServersRepository
import java.util.concurrent.TimeUnit

@HiltWorker
class ReviseVpnServersWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val serversRepository: ServersRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            serversRepository.unblockAvailableVpnServerFromBlacklistRandomly()
            serversRepository.blockUnreachableVpnServers()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val SERVERS_REVISION_WORKER = "servers_revision"

        private const val UNBLOCKING_TIME_INTERVAL = 1L
        private val unblockingWorkerConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val WorkRequest =
            PeriodicWorkRequestBuilder<ReviseVpnServersWorker>(
                repeatInterval = UNBLOCKING_TIME_INTERVAL,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
            )
            .setConstraints(unblockingWorkerConstraints)
            .build()
    }
}
