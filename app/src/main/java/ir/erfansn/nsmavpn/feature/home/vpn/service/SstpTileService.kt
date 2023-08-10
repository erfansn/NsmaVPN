package ir.erfansn.nsmavpn.feature.home.vpn.service

import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._ROOT_STATE

@RequiresApi(Build.VERSION_CODES.N)
class SstpTileService : TileService() {

    private val listener by lazy {
        // TODO: Collect datastore for detecting connection inside app
        /*SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == ROOT_STATE) {
                updateTileState()
            }
        }*/
    }

    private val rootState: Boolean
        get() = _ROOT_STATE

    private val isVpnPrepared: Boolean
        get() = VpnService.prepare(this) == null

    private fun invalidateTileState() {
        qsTile.state = Tile.STATE_UNAVAILABLE
        qsTile.updateTile()
    }

    private fun updateTileState() {
        qsTile.state = if (rootState) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }

        qsTile.updateTile()
    }

    private fun flipTileState() {
        qsTile.state = if (qsTile.state == Tile.STATE_ACTIVE) {
            Tile.STATE_INACTIVE
        } else {
            Tile.STATE_ACTIVE
        }

        qsTile.updateTile()
    }

    private fun initializeState() {
        if (isVpnPrepared) {
            updateTileState()
        } else {
            invalidateTileState()
        }
    }

    override fun onTileAdded() {
        initializeState()
    }

    override fun onStartListening() {
        initializeState()
    }

    private fun startVpnService(action: String) {
        val intent = Intent(this, SstpVpnService::class.java).setAction(action)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onClick() {
        if (!isVpnPrepared /* && server is not cached */) return

        flipTileState()

        when (qsTile.state) {
            Tile.STATE_ACTIVE -> startVpnService(ACTION_VPN_CONNECT)
            Tile.STATE_INACTIVE -> startVpnService(ACTION_VPN_DISCONNECT)
        }
    }
}
