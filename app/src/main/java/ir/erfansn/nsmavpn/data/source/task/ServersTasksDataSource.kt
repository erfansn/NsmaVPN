package ir.erfansn.nsmavpn.data.source.task

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import ir.erfansn.nsmavpn.data.worker.CollectVpnServersWorker
import javax.inject.Inject
import ir.erfansn.nsmavpn.data.sync.worker.ServerUnblockingWorker

class ServersTasksDataSource @Inject constructor(
    private val workManager: WorkManager
) {
    fun collectVpnServerPeriodically() {
        workManager.enqueueUniquePeriodicWork(
            CollectVpnServersWorker.SERVER_COLLECTOR_WORKER,
            ExistingPeriodicWorkPolicy.KEEP,
            CollectVpnServersWorker.WorkRequest
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
