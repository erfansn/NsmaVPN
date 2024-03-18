package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.control

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.core.content.getSystemService
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge

class NetworkObserver(
    val bridge: ClientBridge,
    val startConnectionValidation: () -> Unit,
    val cancelConnectionValidation: () -> Unit,
) {

    private val manager = bridge.service.getSystemService<ConnectivityManager>()!!
    private val callback: ConnectivityManager.NetworkCallback

    init {
        val request = NetworkRequest.Builder().let {
            it.addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            it.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            it.build()
        }

        callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                startConnectionValidation()
            }
        }

        manager.registerNetworkCallback(request, callback)
    }

    fun close() {
        cancelConnectionValidation()

        try {
            manager.unregisterNetworkCallback(callback)
        } catch (_: IllegalArgumentException) {
            Log.i(TAG, "Callback was already unregistered")
        }
    }

    companion object {
        private val TAG = NetworkObserver::class.simpleName
    }
}
