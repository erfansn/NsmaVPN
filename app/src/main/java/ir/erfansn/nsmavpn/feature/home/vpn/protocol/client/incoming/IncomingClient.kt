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
package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.incoming

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.MAX_MRU
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ControlMessage
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Result
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.SstpClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Where
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.ChapClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.IpcpClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.Ipv6cpClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.LcpClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.PapClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp.PppClient
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.probeByte
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.probeShort
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.toIntAsUShort
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.ChapFrame
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.Frame
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.IpcpConfigureFrame
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.Ipv6cpConfigureFrame
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPConfigureFrame
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPEchoRequest
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PAPFrame
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PPP_HEADER
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PPP_PROTOCOL_CHAP
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PPP_PROTOCOL_IP
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PPP_PROTOCOL_IPCP
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PPP_PROTOCOL_IPv6
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PPP_PROTOCOL_IPv6CP
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PPP_PROTOCOL_LCP
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.PPP_PROTOCOL_PAP
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.ControlPacket
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_PACKET_TYPE_CONTROL
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_PACKET_TYPE_DATA
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SstpEchoRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

private const val SSTP_ECHO_INTERVAL = 20_000L
private const val PPP_ECHO_INTERVAL = 20_000L

class IncomingClient(val bridge: ClientBridge) {
    private val bufferSize = bridge.sslTerminal!!.getApplicationBufferSize() + MAX_MRU + 8 // MAX_MRU + 8 for fragment

    private var jobMain: Job? = null

    var lcpMailbox: Channel<LCPConfigureFrame>? = null
    var papMailbox: Channel<PAPFrame>? = null
    var chapMailbox: Channel<ChapFrame>? = null
    var ipcpMailbox: Channel<IpcpConfigureFrame>? = null
    var ipv6cpMailbox: Channel<Ipv6cpConfigureFrame>? = null
    var pppMailbox: Channel<Frame>? = null
    var sstpMailbox: Channel<ControlPacket>? = null

    private val sstpTimer = EchoTimer(SSTP_ECHO_INTERVAL) {
        SstpEchoRequest().also {
            bridge.sslTerminal!!.sendDataUnit(it)
        }
    }

    private val pppTimer = EchoTimer(PPP_ECHO_INTERVAL) {
        LCPEchoRequest().also {
            it.id = bridge.allocateNewFrameID()
            it.holder = "Abura Mashi Mashi".toByteArray(Charsets.US_ASCII)
            bridge.sslTerminal!!.sendDataUnit(it)
        }
    }

    fun <T> registerMailbox(client: T) {
        when (client) {
            is LcpClient -> lcpMailbox = client.mailbox
            is PapClient -> papMailbox = client.mailbox
            is ChapClient -> chapMailbox = client.mailbox
            is IpcpClient -> ipcpMailbox = client.mailbox
            is Ipv6cpClient -> ipv6cpMailbox = client.mailbox
            is PppClient -> pppMailbox = client.mailbox
            is SstpClient -> sstpMailbox = client.mailbox
            else -> throw NotImplementedError(client?.toString() ?: "")
        }
    }

    fun <T> unregisterMailbox(client: T) {
        when (client) {
            is LcpClient -> lcpMailbox = null
            is PapClient -> papMailbox = null
            is ChapClient -> chapMailbox = null
            is IpcpClient -> ipcpMailbox = null
            is Ipv6cpClient -> ipv6cpMailbox = null
            is PppClient -> pppMailbox = null
            is SstpClient -> sstpMailbox = null
            else -> throw NotImplementedError(client?.toString() ?: "")
        }
    }

    fun launchJobMain() {
        jobMain = bridge.service.serviceScope.launch(bridge.handler) {
            val buffer = ByteBuffer.allocate(bufferSize).also { it.limit(0) }

            sstpTimer.tick()
            pppTimer.tick()

            while (isActive) {
                if (!sstpTimer.checkAlive()) {
                    bridge.controlMailbox.send(
                        ControlMessage(Where.SSTP_CONTROL, Result.ERR_TIMEOUT)
                    )

                    return@launch
                }

                if (!pppTimer.checkAlive()) {
                    bridge.controlMailbox.send(
                        ControlMessage(Where.PPP, Result.ERR_TIMEOUT)
                    )

                    return@launch
                }


                val size = getPacketSize(buffer)
                when (size) {
                    in 4..bufferSize -> { }

                    -1 -> {
                        bridge.sslTerminal!!.receive(buffer)
                        continue
                    }

                    else -> {
                        bridge.controlMailbox.send(
                            ControlMessage(Where.INCOMING, Result.ERR_INVALID_PACKET_SIZE)
                        )
                        return@launch
                    }
                }

                if (size > buffer.remaining()) {
                    bridge.sslTerminal!!.receive(buffer)
                    continue
                }

                sstpTimer.tick()

                when (buffer.probeShort(0)) {
                    SSTP_PACKET_TYPE_DATA -> {
                        if (buffer.probeShort(4) != PPP_HEADER) {
                            bridge.controlMailbox.send(
                                ControlMessage(Where.SSTP_DATA, Result.ERR_UNKNOWN_TYPE)
                            )
                            return@launch
                        }

                        pppTimer.tick()

                        val protocol = buffer.probeShort(6)


                        // DATA
                        if (protocol == PPP_PROTOCOL_IP) {
                            processIPPacket(bridge.PPP_IPv4_ENABLED, size, buffer)
                            continue
                        }

                        if (protocol == PPP_PROTOCOL_IPv6) {
                            processIPPacket(bridge.PPP_IPv6_ENABLED, size, buffer)
                            continue
                        }
                        

                        // CONTROL
                        val code = buffer.probeByte(8)
                        val isGo = when (protocol) {
                            PPP_PROTOCOL_LCP -> processLcpFrame(code, buffer)
                            PPP_PROTOCOL_PAP -> processPAPFrame(code, buffer)
                            PPP_PROTOCOL_CHAP -> processChapFrame(code, buffer)
                            PPP_PROTOCOL_IPCP -> processIpcpFrame(code, buffer)
                            PPP_PROTOCOL_IPv6CP -> processIpv6cpFrame(code, buffer)
                            else -> processUnknownProtocol(protocol, size, buffer)
                        }

                        if (!isGo) {
                            return@launch
                        }
                    }

                    SSTP_PACKET_TYPE_CONTROL -> {
                        if (!processControlPacket(buffer.probeShort(4), buffer)) {
                            return@launch
                        }
                    }

                    else -> {
                        bridge.controlMailbox.send(
                            ControlMessage(Where.INCOMING, Result.ERR_UNKNOWN_TYPE)
                        )

                        return@launch
                    }
                }
            }
        }
    }

    private fun getPacketSize(buffer: ByteBuffer): Int {
        return if (buffer.remaining() < 4) {
            -1
        } else {
            buffer.probeShort(2).toIntAsUShort()
        }
    }

    fun cancel() {
        jobMain?.cancel()
    }
}
