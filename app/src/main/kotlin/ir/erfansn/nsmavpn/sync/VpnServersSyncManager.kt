/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
