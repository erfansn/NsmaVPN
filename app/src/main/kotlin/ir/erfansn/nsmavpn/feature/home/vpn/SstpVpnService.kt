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
package ir.erfansn.nsmavpn.feature.home.vpn

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.erfansn.nsmavpn.core.NsmaVpnNotificationManager
import ir.erfansn.nsmavpn.data.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import javax.inject.Inject

@AndroidEntryPoint
class SstpVpnService : VpnService() {

    lateinit var serviceScope: CoroutineScope

    private var vpnStartingJob: Job? = null
    private var disconnectJob: Job? = null
    private var deactivationCheckerJob: Job? = null

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var sstpVpnEventHandler: SstpVpnEventHandler

    @Inject
    lateinit var nsmaVpnNotificationManager: NsmaVpnNotificationManager

    private val localBinder: LocalBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        sstpVpnEventHandler.connectionState
            .onEach {
                if (prepare(this) != null && it !is ConnectionState.Error) return@onEach
                nsmaVpnNotificationManager.notifyOrUpdateNotification(connectionState = it)
            }
            .launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_VPN_CONNECT -> {
                disconnectJob?.cancel()
                notifyForeground()

                deactivationCheckerJob?.cancel()
                deactivationCheckerJob = serviceScope.launch {
                    while (true) {
                        if (prepare(applicationContext) != null) {
                            onRevoke()
                            break
                        }
                        yield()
                    }
                }
                vpnStartingJob?.cancel()
                vpnStartingJob = networkMonitor.isOnline
                    .onEachLatest { isOnline ->
                        check(isOnline)

                        sstpVpnEventHandler.startConnecting()
                    }
                    .catch {
                        sstpVpnEventHandler.disconnectVpnDue(ConnectionState.Error.Network)
                    }
                    .launchIn(serviceScope)

                Service.START_REDELIVER_INTENT
            }

            ACTION_VPN_DISCONNECT -> {
                val quietly = intent.getBooleanExtra(EXTRA_QUIET, false)
                disconnectJob = serviceScope.launch(Dispatchers.Main.immediate) {
                    vpnStartingJob?.cancel()
                    deactivationCheckerJob?.cancel()

                    sstpVpnEventHandler.shutdownVpnTunnel(quietly)
                    stopService()
                }
                Service.START_NOT_STICKY
            }

            else -> {
                throw IllegalStateException()
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun notifyForeground() {
        nsmaVpnNotificationManager.cancelErrorNotification()
        ServiceCompat.startForeground(
            this,
            FOREGROUND_NOTIFICATION_ID,
            nsmaVpnNotificationManager.initialNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
        )
        sstpVpnEventHandler.initState()
    }

    override fun onBind(intent: Intent?): IBinder {
        return super.onBind(intent) ?: localBinder
    }

    override fun onRevoke() {
        sstpVpnEventHandler.disconnectVpnDue(ConnectionState.Error.System)
    }

    private suspend fun stopService() {
        // Due to the lack of guarantee that the notification will not appear after [stopService] is called
        delay(150)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        sstpVpnEventHandler.cleanUp()

        serviceScope.cancel()
    }

    inner class LocalBinder : Binder() {
        val sstpVpnServiceState get() = sstpVpnEventHandler
            .connectionState
            .map {
                SstpVpnServiceState(connectionState = it)
            }
    }

    companion object {
        const val ACTION_VPN_CONNECT =
            "ir.erfansn.nsmavpn.feature.home.vpn.service.action.VPN_CONNECT"

        const val ACTION_VPN_DISCONNECT =
            "ir.erfansn.nsmavpn.feature.home.vpn.service.action.VPN_DISCONNECT"
        const val EXTRA_QUIET =
            "ir.erfansn.nsmavpn.feature.home.vpn.service.extra.QUIET"

        private const val FOREGROUND_NOTIFICATION_ID = NsmaVpnNotificationManager.CONNECTION_NOTIFICATION_ID
    }
}

data class SstpVpnServiceState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
) {
    val started: Boolean
        get() = connectionState is ConnectionState.Connected || connectionState in listOf(
            ConnectionState.Connecting,
            ConnectionState.Validating,
        )
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun <T> Flow<T>.onEachLatest(action: suspend (T) -> Unit): Flow<T> = transformLatest { value ->
    action(value)
    return@transformLatest emit(value)
}
