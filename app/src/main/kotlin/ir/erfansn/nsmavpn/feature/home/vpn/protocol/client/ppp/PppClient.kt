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
package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ControlMessage
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Result
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Where
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.Frame
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPCodeReject
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPEchoReply
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPEchoRequest
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPProtocolReject
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPTerminalAck
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPTerminalRequest
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LcpDiscardRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PppClient(val bridge: ClientBridge) {
    val mailbox = Channel<Frame>(Channel.BUFFERED)

    private var jobControl: Job? = null

    suspend fun launchJobControl() {
        jobControl = bridge.service.serviceScope.launch(bridge.handler) {
            while (isActive) {
                when (val received = mailbox.receive()) {
                    is LCPEchoRequest -> {
                        LCPEchoReply().also {
                            it.id = received.id
                            it.holder = "ErfanSn".toByteArray(Charsets.US_ASCII)
                            bridge.sslTerminal!!.sendDataUnit(it)
                        }
                    }

                    is LCPEchoReply -> { }

                    is LcpDiscardRequest -> { }

                    is LCPTerminalRequest -> {
                        LCPTerminalAck().also {
                            it.id = received.id
                            bridge.sslTerminal!!.sendDataUnit(it)
                        }

                        bridge.controlMailbox.send(
                            ControlMessage(Where.PPP, Result.ERR_TERMINATE_REQUESTED)
                        )
                    }

                    is LCPProtocolReject -> {
                        bridge.controlMailbox.send(
                            ControlMessage(Where.PPP, Result.ERR_PROTOCOL_REJECTED)
                        )
                    }

                    is LCPCodeReject -> {
                        bridge.controlMailbox.send(
                            ControlMessage(Where.PPP, Result.ERR_CODE_REJECTED)
                        )
                    }

                    else -> {
                        bridge.controlMailbox.send(
                            ControlMessage(Where.PPP, Result.ERR_UNEXPECTED_MESSAGE)
                        )
                    }
                }
            }
        }
    }

    fun cancel() {
        jobControl?.cancel()
        mailbox.close()
    }
}
