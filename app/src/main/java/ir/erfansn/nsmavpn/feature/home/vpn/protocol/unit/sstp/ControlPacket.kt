package ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.debug.ParsingDataUnitException
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.debug.assertAlways
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.toIntAsUShort
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.DataUnit
import java.nio.ByteBuffer

const val SSTP_PACKET_TYPE_DATA: Short = 0x1000
const val SSTP_PACKET_TYPE_CONTROL: Short = 0x1001

const val SSTP_MESSAGE_TYPE_CALL_CONNECT_REQUEST: Short = 1
const val SSTP_MESSAGE_TYPE_CALL_CONNECT_ACK: Short = 2
const val SSTP_MESSAGE_TYPE_CALL_CONNECT_NAK: Short = 3
const val SSTP_MESSAGE_TYPE_CALL_CONNECTED: Short = 4
const val SSTP_MESSAGE_TYPE_CALL_ABORT: Short = 5
const val SSTP_MESSAGE_TYPE_CALL_DISCONNECT: Short = 6
const val SSTP_MESSAGE_TYPE_CALL_DISCONNECT_ACK: Short = 7
const val SSTP_MESSAGE_TYPE_ECHO_REQUEST: Short = 8
const val SSTP_MESSAGE_TYPE_ECHO_RESPONSE: Short = 9

abstract class ControlPacket : DataUnit {
    abstract val type: Short
    abstract val numAttribute: Int

    protected var givenLength = 0
    protected var givenNumAttribute = 0

    protected fun readHeader(buffer: ByteBuffer) {
        assertAlways(buffer.short == SSTP_PACKET_TYPE_CONTROL)
        givenLength = buffer.short.toIntAsUShort()
        assertAlways(buffer.short == type)
        givenNumAttribute = buffer.short.toIntAsUShort()
    }

    protected fun writeHeader(buffer: ByteBuffer) {
        buffer.putShort(SSTP_PACKET_TYPE_CONTROL)
        buffer.putShort(length.toShort())
        buffer.putShort(type)
        buffer.putShort(numAttribute.toShort())
    }
}

class SstpCallConnectRequest : ControlPacket() {
    override val type = SSTP_MESSAGE_TYPE_CALL_CONNECT_REQUEST
    override val length = 14
    override val numAttribute = 1

    var protocol = EncapsulatedProtocolId()

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)
        assertAlways(givenLength == length)
        assertAlways(givenNumAttribute == numAttribute)

        protocol.read(buffer)
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        protocol.write(buffer)
    }
}

class SstpCallConnectAck : ControlPacket() {
    override val type = SSTP_MESSAGE_TYPE_CALL_CONNECT_ACK
    override val length = 48
    override val numAttribute = 1

    var request = CryptoBindingRequest()

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)
        assertAlways(givenLength == length)
        assertAlways(givenNumAttribute == numAttribute)

        request.read(buffer)
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        request.write(buffer)
    }
}

class SstpCallConnectNak : ControlPacket() {
    override val type = SSTP_MESSAGE_TYPE_CALL_CONNECT_NAK
    override val length: Int
        get() = 8 + statusInfos.fold(0) {sum, info -> sum + info.length } + holder.size

    override val numAttribute: Int
        get() = statusInfos.size

    val statusInfos = mutableListOf<StatusInfo>()

    var holder = ByteArray(0)

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        repeat(givenNumAttribute) {
            StatusInfo().also {
                it.read(buffer)
                statusInfos.add(it)
            }
        }

        val holderSize = givenLength - length
        assertAlways(holderSize >= 0)

        if (holderSize > 0) {
            holder = ByteArray(holderSize).also { buffer.get(it) }
        }
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        statusInfos.forEach {
            it.write(buffer)
        }

        buffer.put(holder)
    }
}

class SstpCallConnected : ControlPacket() {
    override val type = SSTP_MESSAGE_TYPE_CALL_CONNECTED
    override val length = 112
    override val numAttribute = 1

    var binding = CryptoBinding()

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)
        assertAlways(givenLength == length)
        assertAlways(givenNumAttribute == numAttribute)

        binding.read(buffer)
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        binding.write(buffer)
    }
}

abstract class TerminatePacket : ControlPacket() {
    override val length: Int
        get() = 8 + (statusInfo?.length ?: 0)

    override val numAttribute: Int
        get() = statusInfo?.let { 1 } ?: 0

    var statusInfo: StatusInfo? = null

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        statusInfo = when (givenNumAttribute) {
            0 -> null
            1 -> StatusInfo().also { it.read(buffer) }
            else -> throw ParsingDataUnitException()
        }

        assertAlways(givenLength == length)
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        statusInfo?.also {
            it.write(buffer)
        }
    }
}

class SstpCallAbort : TerminatePacket() {
    override val type = SSTP_MESSAGE_TYPE_CALL_ABORT
}

class SstpCallDisconnect : TerminatePacket() {
    override val type = SSTP_MESSAGE_TYPE_CALL_DISCONNECT
}

abstract class NoAttributePacket : ControlPacket() {
    override val length = 8
    override val numAttribute = 0

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)
    }
}

class SstpCallDisconnectAck : NoAttributePacket() {
    override val type = SSTP_MESSAGE_TYPE_CALL_DISCONNECT_ACK
}

class SstpEchoRequest : NoAttributePacket() {
    override val type = SSTP_MESSAGE_TYPE_ECHO_REQUEST
}

class SstpEchoResponse : NoAttributePacket() {
    override val type = SSTP_MESSAGE_TYPE_ECHO_RESPONSE
}
