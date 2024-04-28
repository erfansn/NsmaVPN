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
package ir.erfansn.nsmavpn.feature.home

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import dagger.hilt.android.AndroidEntryPoint
import ir.erfansn.nsmavpn.data.model.isEmpty
import ir.erfansn.nsmavpn.data.repository.UserProfileRepository
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnService
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnServiceAction
import ir.erfansn.nsmavpn.sync.VpnServersSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class VpnSwitchTileService : TileService() {

    private lateinit var serviceScope: CoroutineScope

    private val vpnServiceStarted = MutableStateFlow(false)
    private val serviceConnection = object : ServiceConnection {

        private var job: Job? = null

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service !is SstpVpnService.LocalBinder) return

            job = service.sstpVpnServiceState
                .onEach { state ->
                    vpnServiceStarted.update { state.started }
                }
                .launchIn(serviceScope)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            job?.cancel()
        }
    }

    private var vpnServiceStartedCollectorJob: Job? = null

    @Inject
    lateinit var vpnServersSyncManager: VpnServersSyncManager
    @Inject
    lateinit var sstpVpnServiceAction: SstpVpnServiceAction
    @Inject
    lateinit var userProfileRepository: UserProfileRepository

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        bindService(
            Intent(this, SstpVpnService::class.java),
            serviceConnection,
            Service.BIND_AUTO_CREATE
        )
    }

    override fun onStartListening() {
        super.onStartListening()
        vpnServiceStartedCollectorJob = combine(
                vpnServiceStarted,
                vpnServersSyncManager.isSyncing,
                userProfileRepository.userProfile,
            ) { started, syncing, userProfile ->
                qsTile.state = when {
                    userProfile.isEmpty() || !isVpnPrepared || syncing -> Tile.STATE_UNAVAILABLE
                    isVpnPrepared && started -> Tile.STATE_ACTIVE
                    else -> Tile.STATE_INACTIVE
                }
            }
            .onEach {
                qsTile.updateTile()
            }
            .launchIn(serviceScope)
    }

    private val isVpnPrepared: Boolean
        get() = VpnService.prepare(this) == null

    override fun onClick() {
        super.onClick()
        when (qsTile.state) {
            Tile.STATE_ACTIVE -> {
                sstpVpnServiceAction.disconnect()
                qsTile.state = Tile.STATE_INACTIVE
            }

            Tile.STATE_INACTIVE -> {
                sstpVpnServiceAction.connect()
                qsTile.state = Tile.STATE_ACTIVE
            }
        }
        qsTile.updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        vpnServiceStartedCollectorJob?.cancel()

        qsTile.state = Tile.STATE_UNAVAILABLE
        qsTile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        serviceScope.cancel()
    }
}
