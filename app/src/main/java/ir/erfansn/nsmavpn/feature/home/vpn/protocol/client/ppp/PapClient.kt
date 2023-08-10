package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ControlMessage
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Result
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Where
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PAPAuthenticateRequest
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PAPFrame
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PAP_CODE_AUTHENTICATE_ACK
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PAP_CODE_AUTHENTICATE_NAK
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PapClient(private val bridge: ClientBridge) {
    val mailbox = Channel<PAPFrame>(Channel.BUFFERED)
    private var jobAuth: Job? = null

    fun launchJobAuth() {
        jobAuth = bridge.service.scope.launch(bridge.handler) {
            val currentID = bridge.allocateNewFrameID()

            sendPAPRequest(currentID)

            while (isActive) {
                val received = mailbox.receive()

                if (received.id != currentID) continue

                when (received.code) {
                    PAP_CODE_AUTHENTICATE_ACK -> {
                        bridge.controlMailbox.send(
                            ControlMessage(Where.PAP, Result.PROCEEDED)
                        )
                    }

                    PAP_CODE_AUTHENTICATE_NAK -> {
                        bridge.controlMailbox.send(
                            ControlMessage(Where.PAP, Result.ERR_AUTHENTICATION_FAILED)
                        )
                    }
                }
            }
        }
    }

    private suspend fun sendPAPRequest(id: Byte) {
        PAPAuthenticateRequest().also {
            it.id = id
            it.idFiled = bridge.HOME_USERNAME.toByteArray(Charsets.US_ASCII)
            it.passwordFiled = bridge.HOME_PASSWORD.toByteArray(Charsets.US_ASCII)

            bridge.sslTerminal!!.sendDataUnit(it)
        }
    }

    fun cancel() {
        jobAuth?.cancel()
        mailbox.close()
    }
}
