package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.control

import android.util.Log
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.OscPrefKey
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ControlMessage
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.OutgoingClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Result
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.SSTP_REQUEST_TIMEOUT
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.SstpClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Where
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.incoming.IncomingClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.ChapClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.IpcpClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.Ipv6cpClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.LcpClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.PPP_NEGOTIATION_TIMEOUT
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.PapClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.PppClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.debug.assertAlways
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.terminal.SSL_REQUEST_INTERVAL
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.AuthOptionMSChapv2
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.AuthOptionPAP
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_MESSAGE_TYPE_CALL_ABORT
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_MESSAGE_TYPE_CALL_DISCONNECT
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_MESSAGE_TYPE_CALL_DISCONNECT_ACK
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeoutOrNull

class ControlClient(
    val bridge: ClientBridge,
    private val onRestartVpn: suspend () -> Unit,
    private val onStartConnectionValidation: () -> Unit,
    private val onCancelConnectionValidation: () -> Unit,
) {
    private var observer: NetworkObserver? = null

    private var sstpClient: SstpClient? = null
    private var pppClient: PppClient? = null
    private var incomingClient: IncomingClient? = null
    private var outgoingClient: OutgoingClient? = null

    private var lcpClient: LcpClient? = null
    private var papClient: PapClient? = null
    private var chapClient: ChapClient? = null
    private var ipcpClient: IpcpClient? = null
    private var ipv6cpClient: Ipv6cpClient? = null

    private var jobMain: Job? = null

    private val mutex = Mutex()

    private fun attachHandler() {
        bridge.handler = CoroutineExceptionHandler { _, throwable ->
            kill(isReconnectionRequested = true) {
                Log.e("ControlClient", throwable.message, throwable)
            }
        }
    }

    fun launchJobMain() {
        attachHandler()

        jobMain = bridge.service.serviceScope.launch(bridge.handler) {
            bridge.attachSSLTerminal()
            bridge.attachIPTerminal()

            bridge.sslTerminal!!.initialize()
            if (!expectProceeded(Where.SSL, SSL_REQUEST_INTERVAL)) {
                return@launch
            }

            IncomingClient(bridge).also {
                it.launchJobMain()
                incomingClient = it
            }

            SstpClient(bridge).also {
                sstpClient = it
                incomingClient!!.registerMailbox(it)
                it.launchJobRequest()

                if (!expectProceeded(Where.SSTP_REQUEST, SSTP_REQUEST_TIMEOUT)) {
                    return@launch
                }

                sstpClient!!.launchJobControl()
            }

            PppClient(bridge).also {
                pppClient = it
                incomingClient!!.registerMailbox(it)
                it.launchJobControl()
            }

            LcpClient(bridge).also {
                incomingClient!!.registerMailbox(it)
                it.launchJobNegotiation()

                if (!expectProceeded(Where.LCP, PPP_NEGOTIATION_TIMEOUT)) {
                    return@launch
                }

                incomingClient!!.unregisterMailbox(it)
            }

            val authTimeout = OscPrefKey.PPP_AUTH_TIMEOUT * 1000L
            when (bridge.currentAuth) {
                is AuthOptionPAP -> PapClient(bridge).also {
                    incomingClient!!.registerMailbox(it)
                    it.launchJobAuth()

                    if (!expectProceeded(Where.PAP, authTimeout)) {
                        return@launch
                    }

                    incomingClient!!.unregisterMailbox(it)
                }

                is AuthOptionMSChapv2 -> ChapClient(bridge).also {
                    chapClient = it
                    incomingClient!!.registerMailbox(it)
                    it.launchJobAuth()

                    if (!expectProceeded(Where.CHAP, authTimeout)) {
                        return@launch
                    }
                }

                else -> throw NotImplementedError(bridge.currentAuth.protocol.toString())
            }

            sstpClient!!.sendCallConnected()

            if (bridge.PPP_IPv4_ENABLED) {
                IpcpClient(bridge).also {
                    incomingClient!!.registerMailbox(it)
                    it.launchJobNegotiation()

                    if (!expectProceeded(Where.IPCP, PPP_NEGOTIATION_TIMEOUT)) {
                        return@launch
                    }

                    incomingClient!!.unregisterMailbox(it)
                }
            }

            if (bridge.PPP_IPv6_ENABLED) {
                Ipv6cpClient(bridge).also {
                    incomingClient!!.registerMailbox(it)
                    it.launchJobNegotiation()

                    if (!expectProceeded(Where.IPV6CP, PPP_NEGOTIATION_TIMEOUT)) {
                        return@launch
                    }

                    incomingClient!!.unregisterMailbox(it)
                }
            }

            bridge.ipTerminal!!.initialize()
            if (!expectProceeded(Where.IP, null)) {
                return@launch
            }

            OutgoingClient(bridge).also {
                it.launchJobMain()
                outgoingClient = it
            }

            observer = NetworkObserver(
                bridge = bridge,
                startConnectionValidation = onStartConnectionValidation,
                cancelConnectionValidation = onCancelConnectionValidation,
            )

            expectProceeded(Where.SSTP_CONTROL, null) // wait ERR_ message until disconnection
        }
    }

    private suspend fun expectProceeded(where: Where, timeout: Long?): Boolean {
        val received = if (timeout != null) {
            withTimeoutOrNull(timeout) {
                bridge.controlMailbox.receive()
            } ?: ControlMessage(where, Result.ERR_TIMEOUT)
        } else {
            bridge.controlMailbox.receive()
        }

        if (received.result == Result.PROCEEDED) {
            assertAlways(received.from == where)
            return true
        }

        val lastPacketType = if (received.result == Result.ERR_DISCONNECT_REQUESTED) {
            SSTP_MESSAGE_TYPE_CALL_DISCONNECT_ACK
        } else {
            SSTP_MESSAGE_TYPE_CALL_ABORT
        }

        kill(isReconnectionRequested = true) {
            sstpClient?.sendLastPacket(lastPacketType)
        }
        return false
    }

    fun disconnect() { // use if the user want to normally disconnect
        kill {
            sstpClient?.sendLastPacket(SSTP_MESSAGE_TYPE_CALL_DISCONNECT)
        }
    }

    private fun kill(
        isReconnectionRequested: Boolean = false,
        cleanup: (suspend () -> Unit)? = null
    ) {
        if (!mutex.tryLock()) return

        bridge.service.serviceScope.launch {
            observer?.close()

            jobMain?.cancel()
            cancelClients()

            cleanup?.invoke()

            closeTerminals()

            if (isReconnectionRequested) {
                onRestartVpn()
            }
        }
    }

    fun cleanUp() {
        kill()
        // Never ever call stopService here because cause destroying service
        // after configuration changes
    }

    private fun cancelClients() {
        lcpClient?.cancel()
        papClient?.cancel()
        chapClient?.cancel()
        ipcpClient?.cancel()
        ipv6cpClient?.cancel()
        sstpClient?.cancel()
        pppClient?.cancel()
        incomingClient?.cancel()
        outgoingClient?.cancel()
    }

    private fun closeTerminals() {
        bridge.sslTerminal?.close()
        bridge.ipTerminal?.close()
    }
}
