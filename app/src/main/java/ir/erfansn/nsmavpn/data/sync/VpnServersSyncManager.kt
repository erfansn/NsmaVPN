package ir.erfansn.nsmavpn.data.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import ir.erfansn.nsmavpn.data.sync.worker.CollectVpnServersWorker
import ir.erfansn.nsmavpn.data.sync.worker.ReviseVpnServersWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultVpnServersSyncManager @Inject constructor(
    private val workManager: WorkManager
) : VpnServersSyncManager {

    override val isSyncing =
        workManager.getWorkInfosForUniqueWorkFlow(CollectVpnServersWorker.SERVER_COLLECTOR_WORKER)
            .filter { !it.isNullOrEmpty() }
            .map(List<WorkInfo>::first)
            .map { !it.state.isFinished }
            .conflate()

    override fun beginVpnServersSyncTasks() {
        collectVpnServerPeriodically()
        reviseAvailableVpnServersPeriodically()
    }

    private fun collectVpnServerPeriodically() {
        workManager.enqueueUniquePeriodicWork(
            CollectVpnServersWorker.SERVER_COLLECTOR_WORKER,
            ExistingPeriodicWorkPolicy.KEEP,
            CollectVpnServersWorker.WorkRequest
        )
    }

    private fun reviseAvailableVpnServersPeriodically() {
        workManager.enqueueUniquePeriodicWork(
            ReviseVpnServersWorker.SERVERS_REVISION_WORKER,
            ExistingPeriodicWorkPolicy.KEEP,
            ReviseVpnServersWorker.WorkRequest
        )
    }

    override fun endAllVpnServersSyncTasks() {
        workManager.cancelAllWork()
    }
}

interface VpnServersSyncManager {
    val isSyncing: Flow<Boolean>
    fun beginVpnServersSyncTasks()
    fun endAllVpnServersSyncTasks()
}
