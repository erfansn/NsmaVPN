package ir.erfansn.nsmavpn.data.source.task

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import ir.erfansn.nsmavpn.data.worker.ServerCollectorWorker
import ir.erfansn.nsmavpn.data.worker.ServerUnblockingWorker
import javax.inject.Inject

class ServersTasksDataSource @Inject constructor(
    private val workManager: WorkManager
) {
    fun collectVpnServerPeriodically() {
        workManager.enqueueUniquePeriodicWork(
            ServerCollectorWorker.SERVER_COLLECTOR_WORKER,
            ExistingPeriodicWorkPolicy.KEEP,
            ServerCollectorWorker.workRequest
        )
    }

    fun removeAvailableVpnServerFromBlacklistPeriodically() {
        workManager.enqueueUniquePeriodicWork(
            ServerUnblockingWorker.SERVER_UNBLOCKING_WORKER,
            ExistingPeriodicWorkPolicy.KEEP,
            ServerUnblockingWorker.workRequest
        )
    }
}
