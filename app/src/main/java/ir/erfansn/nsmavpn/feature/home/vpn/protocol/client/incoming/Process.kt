package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.incoming

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ControlMessage
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Result
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Where
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.move
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.DataUnit
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.*
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.CHAP_CODE_CHALLENGE
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.CHAP_CODE_FAILURE
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.CHAP_CODE_RESPONSE
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.CHAP_CODE_SUCCESS
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.ChapChallenge
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.ChapFailure
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.ChapResponse
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.ChapSuccess
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_MESSAGE_TYPE_CALL_ABORT
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_MESSAGE_TYPE_CALL_CONNECTED
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_MESSAGE_TYPE_CALL_CONNECT_ACK
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_MESSAGE_TYPE_CALL_CONNECT_NAK
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_MESSAGE_TYPE_CALL_CONNECT_REQUEST
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_MESSAGE_TYPE_CALL_DISCONNECT
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_MESSAGE_TYPE_CALL_DISCONNECT_ACK
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_MESSAGE_TYPE_ECHO_REQUEST
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_MESSAGE_TYPE_ECHO_RESPONSE
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SstpCallAbort
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SstpCallConnectAck
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SstpCallConnectNak
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SstpCallConnectRequest
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SstpCallConnected
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SstpCallDisconnect
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SstpCallDisconnectAck
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SstpEchoRequest
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SstpEchoResponse
import java.nio.ByteBuffer

private suspend fun IncomingClient.tryReadDataUnit(unit: DataUnit, buffer: ByteBuffer): Exception? {
    try {
        unit.read(buffer)
    } catch (e: Exception) { // need to save packet log
        bridge.controlMailbox.send(
            ControlMessage(Where.INCOMING, Result.ERR_PARSING_FAILED)
        )
        return e
    }
    return null
}

suspend fun IncomingClient.processControlPacket(type: Short, buffer: ByteBuffer): Boolean {
    val packet = when (type) {
        SSTP_MESSAGE_TYPE_CALL_CONNECT_REQUEST -> SstpCallConnectRequest()
        SSTP_MESSAGE_TYPE_CALL_CONNECT_ACK -> SstpCallConnectAck()
        SSTP_MESSAGE_TYPE_CALL_CONNECT_NAK -> SstpCallConnectNak()
        SSTP_MESSAGE_TYPE_CALL_CONNECTED -> SstpCallConnected()
        SSTP_MESSAGE_TYPE_CALL_ABORT -> SstpCallAbort()
        SSTP_MESSAGE_TYPE_CALL_DISCONNECT -> SstpCallDisconnect()
        SSTP_MESSAGE_TYPE_CALL_DISCONNECT_ACK -> SstpCallDisconnectAck()
        SSTP_MESSAGE_TYPE_ECHO_REQUEST -> SstpEchoRequest()
        SSTP_MESSAGE_TYPE_ECHO_RESPONSE -> SstpEchoResponse()
        else -> {
            bridge.controlMailbox.send(
                ControlMessage(Where.SSTP_CONTROL, Result.ERR_UNKNOWN_TYPE)
            )

            return false
        }
    }

    tryReadDataUnit(packet, buffer)?.also {
        return false
    }

    sstpMailbox?.send(packet)

    return true
}

suspend fun IncomingClient.processLcpFrame(code: Byte, buffer: ByteBuffer): Boolean {
    if (code in 1..4) {
        val configureFrame = when (code) {
            LCP_CODE_CONFIGURE_REQUEST -> LCPConfigureRequest()
            LCP_CODE_CONFIGURE_ACK -> LCPConfigureAck()
            LCP_CODE_CONFIGURE_NAK -> LCPConfigureNak()
            LCP_CODE_CONFIGURE_REJECT -> LCPConfigureReject()
            else -> throw NotImplementedError()
        }

        tryReadDataUnit(configureFrame, buffer)?.also {
            return false
        }

        lcpMailbox?.send(configureFrame)
        return true
    }

    if (code in 5..11) {
        val frame = when (code) {
            LCP_CODE_TERMINATE_REQUEST -> LCPTerminalRequest()
            LCP_CODE_TERMINATE_ACK -> LCPTerminalAck()
            LCP_CODE_CODE_REJECT -> LCPCodeReject()
            LCP_CODE_PROTOCOL_REJECT -> LCPProtocolReject()
            LCP_CODE_ECHO_REQUEST -> LCPEchoRequest()
            LCP_CODE_ECHO_REPLY -> LCPEchoReply()
            LCP_CODE_DISCARD_REQUEST -> LcpDiscardRequest()
            else -> throw NotImplementedError()
        }

        tryReadDataUnit(frame, buffer)?.also {
            return false
        }

        pppMailbox?.send(frame)
        return true
    }

    bridge.controlMailbox.send(
        ControlMessage(Where.LCP, Result.ERR_UNKNOWN_TYPE)
    )

    return false
}

suspend fun IncomingClient.processPAPFrame(code: Byte, buffer: ByteBuffer): Boolean {
    val frame = when (code) {
        PAP_CODE_AUTHENTICATE_REQUEST -> PAPAuthenticateRequest()
        PAP_CODE_AUTHENTICATE_ACK -> PAPAuthenticateAck()
        PAP_CODE_AUTHENTICATE_NAK -> PAPAuthenticateNak()
        else -> {
            bridge.controlMailbox.send(
                ControlMessage(Where.PAP, Result.ERR_UNKNOWN_TYPE)
            )

            return false
        }
    }

    tryReadDataUnit(frame, buffer)?.also {
        return false
    }

    papMailbox?.send(frame)
    return true
}

suspend fun IncomingClient.processChapFrame(code: Byte, buffer: ByteBuffer): Boolean {
    val frame = when (code) {
        CHAP_CODE_CHALLENGE -> ChapChallenge()
        CHAP_CODE_RESPONSE -> ChapResponse()
        CHAP_CODE_SUCCESS -> ChapSuccess()
        CHAP_CODE_FAILURE -> ChapFailure()
        else -> {
            bridge.controlMailbox.send(
                ControlMessage(Where.CHAP, Result.ERR_UNKNOWN_TYPE)
            )

            return false
        }
    }

    tryReadDataUnit(frame, buffer)?.also {
        return false
    }

    chapMailbox?.send(frame)
    return true
}

suspend fun IncomingClient.processIpcpFrame(code: Byte, buffer: ByteBuffer): Boolean {
    val frame = when (code) {
        LCP_CODE_CONFIGURE_REQUEST -> IpcpConfigureRequest()
        LCP_CODE_CONFIGURE_ACK -> IpcpConfigureAck()
        LCP_CODE_CONFIGURE_NAK -> IpcpConfigureNak()
        LCP_CODE_CONFIGURE_REJECT -> IpcpConfigureReject()
        else -> {
            bridge.controlMailbox.send(
                ControlMessage(Where.IPCP, Result.ERR_UNKNOWN_TYPE)
            )

            return false
        }
    }

    tryReadDataUnit(frame, buffer)?.also {
        return false
    }

    ipcpMailbox?.send(frame)
    return true
}

suspend fun IncomingClient.processIpv6cpFrame(code: Byte, buffer: ByteBuffer): Boolean {
    val frame = when (code) {
        LCP_CODE_CONFIGURE_REQUEST -> Ipv6cpConfigureRequest()
        LCP_CODE_CONFIGURE_ACK -> Ipv6cpConfigureAck()
        LCP_CODE_CONFIGURE_NAK -> Ipv6cpConfigureNak()
        LCP_CODE_CONFIGURE_REJECT -> Ipv6cpConfigureReject()
        else -> {
            bridge.controlMailbox.send(
                ControlMessage(Where.IPV6CP, Result.ERR_UNKNOWN_TYPE)
            )

            return false
        }
    }

    tryReadDataUnit(frame, buffer)?.also {
        return false
    }

    ipv6cpMailbox?.send(frame)
    return true
}

suspend fun IncomingClient.processUnknownProtocol(protocol: Short, packetSize: Int, buffer: ByteBuffer): Boolean {
    LCPProtocolReject().also {
        it.rejectedProtocol = protocol
        it.id = bridge.allocateNewFrameID()
        val infoStart = buffer.position() + 8
        val infoStop = buffer.position() + packetSize
        it.holder = buffer.array().sliceArray(infoStart until infoStop)

        bridge.sslTerminal!!.sendDataUnit(it)
    }

    buffer.move(packetSize)

    return true
}

fun IncomingClient.processIPPacket(isEnabledProtocol: Boolean, packetSize: Int, buffer: ByteBuffer) {
    if (isEnabledProtocol) {
        val start = buffer.position() + 8
        val ipPacketSize = packetSize - 8

        bridge.ipTerminal!!.writePacket(start, ipPacketSize, buffer)
    }

    buffer.move(packetSize)
}
