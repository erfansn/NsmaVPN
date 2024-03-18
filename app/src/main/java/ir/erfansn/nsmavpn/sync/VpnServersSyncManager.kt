package ir.erfansn.nsmavpn.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface VpnServersSyncManager {
    val isSyncing: Flow<Boolean>
    fun beginVpnServersSync()
    fun endAllVpnServersSync()
}

class DefaultVpnServersSyncManager @Inject constructor(
    private val workManager: WorkManager
) : VpnServersSyncManager {

    override val isSyncing =
        workManager.getWorkInfosForUniqueWorkFlow(VpnServersSyncWorker.SYNCHRONIZER_WORKER)
            .map(List<WorkInfo>::firstOrNull)
            .map { it?.state == WorkInfo.State.RUNNING }
            .conflate()

    override fun beginVpnServersSync() {
        workManager.enqueueUniquePeriodicWork(
            VpnServersSyncWorker.SYNCHRONIZER_WORKER,
            ExistingPeriodicWorkPolicy.KEEP,
            VpnServersSyncWorker.WorkRequest
        )
    }

    override fun endAllVpnServersSync() {
        workManager.cancelAllWork()
    }
}
