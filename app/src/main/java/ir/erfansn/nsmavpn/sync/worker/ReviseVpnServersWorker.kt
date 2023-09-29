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
class ReviseVpnServersWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val serversRepository: ServersRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            serversRepository.unblockFirstAvailableVpnServerFromBlacklist()
            serversRepository.blockUnreachableVpnServers()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val SERVERS_REVISION_WORKER = "servers_revision"

        private val unblockingWorkerConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val WorkRequest =
            PeriodicWorkRequestBuilder<ReviseVpnServersWorker>(
                repeatInterval = 2,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
            )
            .setConstraints(unblockingWorkerConstraints)
            .build()
    }
}
