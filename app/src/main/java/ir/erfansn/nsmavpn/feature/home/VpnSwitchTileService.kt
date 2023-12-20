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
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnService
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
        vpnServiceStartedCollectorJob = vpnServiceStarted
            .combine(vpnServersSyncManager.isSyncing) { started, syncing ->
                qsTile.state = when {
                    !isVpnPrepared || syncing -> Tile.STATE_UNAVAILABLE
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
                startService(
                    Intent(this, SstpVpnService::class.java).apply {
                        action = SstpVpnService.ACTION_VPN_DISCONNECT
                    }
                )
                qsTile.state = Tile.STATE_INACTIVE
            }

            Tile.STATE_INACTIVE -> {
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, SstpVpnService::class.java).apply {
                        action = SstpVpnService.ACTION_VPN_CONNECT
                    }
                )
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
