package ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.debug.assertAlways
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.LCPOptionPack
import java.nio.ByteBuffer

const val LCP_CODE_CONFIGURE_REQUEST: Byte = 1
const val LCP_CODE_CONFIGURE_ACK: Byte = 2
const val LCP_CODE_CONFIGURE_NAK: Byte = 3
const val LCP_CODE_CONFIGURE_REJECT: Byte = 4
const val LCP_CODE_TERMINATE_REQUEST: Byte = 5
const val LCP_CODE_TERMINATE_ACK: Byte = 6
const val LCP_CODE_CODE_REJECT: Byte = 7
const val LCP_CODE_PROTOCOL_REJECT: Byte = 8
const val LCP_CODE_ECHO_REQUEST: Byte = 9
const val LCP_CODE_ECHO_REPLY: Byte = 10
const val LCP_CODE_DISCARD_REQUEST: Byte = 11

abstract class LCPFrame : Frame() {
    override val protocol = PPP_PROTOCOL_LCP
}

abstract class LCPConfigureFrame : LCPFrame() {
    override val length: Int
        get() = headerSize + options.length

    var options: LCPOptionPack = LCPOptionPack()

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        options = LCPOptionPack(givenLength - length).also {
            it.read(buffer)
        }
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        options.write(buffer)
    }
}

class LCPConfigureRequest : LCPConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_REQUEST
}

class LCPConfigureAck : LCPConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_ACK
}

class LCPConfigureNak : LCPConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_NAK
}

class LCPConfigureReject : LCPConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_REJECT
}

abstract class LCPDataHoldingFrame : LCPFrame() {
    override val length: Int
        get() = headerSize + holder.size

    var holder = ByteArray(0)

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        val holderSize = givenLength - length
        assertAlways(holderSize >= 0)

        if (holderSize > 0) {
            holder = ByteArray(holderSize).also { buffer.get(it) }
        }
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        buffer.put(holder)
    }
}

class LCPTerminalRequest : LCPDataHoldingFrame() {
    override val code = LCP_CODE_TERMINATE_REQUEST
}

class LCPTerminalAck : LCPDataHoldingFrame() {
    override val code = LCP_CODE_TERMINATE_ACK
}

class LCPCodeReject : LCPDataHoldingFrame() {
    override val code = LCP_CODE_CODE_REJECT
}

class LCPProtocolReject : LCPFrame() {
    override val code = LCP_CODE_PROTOCOL_REJECT
    override val length: Int
        get() = headerSize + Short.SIZE_BYTES + holder.size

    var rejectedProtocol: Short = 0

    var holder = ByteArray(0)

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        rejectedProtocol = buffer.short

        val holderSize = givenLength - length
        assertAlways(holderSize >= 0)

        if (holderSize > 0) {
            holder = ByteArray(holderSize).also { buffer.get(it) }
        }
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        buffer.putShort(rejectedProtocol)
        buffer.put(holder)
    }
}


abstract class LCPMagicNumberFrame : LCPFrame() {
    override val length: Int
        get() = headerSize + Int.SIZE_BYTES + holder.size

    var magicNumber = 0

    var holder = ByteArray(0)

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        magicNumber = buffer.int

        val holderSize = givenLength - length
        assertAlways(holderSize >= 0)

        if (holderSize > 0) {
            holder = ByteArray(holderSize).also { buffer.get(it) }
        }
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        buffer.putInt(magicNumber)
        buffer.put(holder)
    }
}

class LCPEchoRequest : LCPMagicNumberFrame() {
    override val code = LCP_CODE_ECHO_REQUEST
}

class LCPEchoReply : LCPMagicNumberFrame() {
    override val code = LCP_CODE_ECHO_REPLY
}

class LcpDiscardRequest : LCPMagicNumberFrame() {
    override val code = LCP_CODE_DISCARD_REQUEST
}
