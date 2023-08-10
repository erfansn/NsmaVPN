package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.control

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.content.getSystemService
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge

class NetworkObserver(val bridge: ClientBridge) {
    private val manager = bridge.service.getSystemService<ConnectivityManager>()
    private val callback: ConnectivityManager.NetworkCallback

    init {
        val request = NetworkRequest.Builder().let {
            it.addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            it.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            it.build()
        }


        callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    /*manager.getLinkProperties(network)?.also {
                        updateSummary(it)
                    }*/
                }
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                // updateSummary(linkProperties)
            }
        }

        manager?.registerNetworkCallback(request, callback)
    }

    fun close() {
        try {
            manager?.unregisterNetworkCallback(callback)
        } catch (_: IllegalArgumentException) {
        } // already unregistered
    }
}
