package ir.erfansn.nsmavpn.feature.home.vpn

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface SstpVpnServiceAction {
    fun connect()
    fun disconnect(quietly: Boolean = false)
}

class DefaultSstpVpnServiceAction @Inject constructor(
    @ApplicationContext private val context: Context
) : SstpVpnServiceAction {

    override fun connect() {
        ContextCompat.startForegroundService(
            context,
            Intent(context, SstpVpnService::class.java).apply {
                action = SstpVpnService.ACTION_VPN_CONNECT
            }
        )
    }

    override fun disconnect(quietly: Boolean) {
        context.startService(
            Intent(context, SstpVpnService::class.java).apply {
                action = SstpVpnService.ACTION_VPN_DISCONNECT
                putExtra(SstpVpnService.EXTRA_QUIET, quietly)
            }
        )
    }
}
