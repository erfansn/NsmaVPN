package ir.erfansn.nsmavpn.data.util.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import java.net.URL
import java.net.URLConnection

class VpnNetworkMonitor(
    context: Context,
) : NetworkMonitor<VpnConnectionStatus> {

    override val status = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                launch(Dispatchers.IO) {
                    val connection = network.openConnection(URL("https://google.com"))
                    connection.connect()
                    // TODO: when vpn connection was valid save vpn server in user preference
                    //  and use it when user try to enable vpn from tiles in notification drawer
                    //  else disconnect the vpn service then block server and request a new one
                    trySend(if (connection.isSuccessful) VpnConnectionStatus.Established else VpnConnectionStatus.Invalid)
                }
            }

            override fun onLost(network: Network) {
                trySend(VpnConnectionStatus.Unknown)
            }
        }

        connectivityManager?.registerNetworkCallback(
            NetworkRequest.Builder()
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                .build(),
            callback,
        )

        trySend(VpnConnectionStatus.Unknown)

        awaitClose {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }.conflate()

    private val URLConnection.isSuccessful: Boolean
        get() = getHeaderField("Status").split(" ")[0].toInt() / 100 == 2
}

enum class VpnConnectionStatus { Established, Invalid, Unknown }
