package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.control

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.core.content.getSystemService
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge
import kotlinx.coroutines.Job

class NetworkObserver(val bridge: ClientBridge) {

    private val manager = bridge.service.getSystemService<ConnectivityManager>()!!
    private val callback: ConnectivityManager.NetworkCallback

    private var validatingJob: Job? = null
    private var firstValidation: Boolean = true

    init {
        val request = NetworkRequest.Builder().let {
            it.addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            it.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            it.build()
        }

        callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                bridge.service.startConnectionValidation()

                /*bridge.service.vpnEventHandler.sendEvent(VpnEvent.Validating)*/
                /*job = with(bridge.service) {
                    serviceScope.launch {
                        checkServerValidation()
                    }
                }*/
                Log.i(TAG, "onAvailable: Called")
            }
        }

        manager.registerNetworkCallback(request, callback)
    }

    fun close() {
        Log.i(TAG, "close: Called")
        // validatingJob?.cancel()
        bridge.service.cancelConnectionValidation()

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
