package ir.erfansn.nsmavpn.data.util.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class InternetNetworkMonitor(
    context: Context,
) : NetworkMonitor<InternetConnectionStatus> {

    override val status = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(InternetConnectionStatus.Online)
            }

            override fun onLost(network: Network) {
                trySend(InternetConnectionStatus.Offline)
            }
        }

        connectivityManager?.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            callback,
        )

        trySend(if (connectivityManager.isCurrentlyConnected()) InternetConnectionStatus.Online else InternetConnectionStatus.Offline)

        awaitClose {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }

    private fun ConnectivityManager?.isCurrentlyConnected() = when (this) {
        null -> false
        else -> activeNetwork
            ?.let(::getNetworkCapabilities)
            ?.let {
                it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
            ?: false
    }
}

enum class InternetConnectionStatus { Online, Offline }
