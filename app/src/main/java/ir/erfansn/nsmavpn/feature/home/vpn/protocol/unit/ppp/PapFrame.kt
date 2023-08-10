package ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.debug.ParsingDataUnitException
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.debug.assertAlways
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.toIntAsUByte
import java.nio.ByteBuffer

const val PAP_CODE_AUTHENTICATE_REQUEST: Byte = 1
const val PAP_CODE_AUTHENTICATE_ACK: Byte = 2
const val PAP_CODE_AUTHENTICATE_NAK: Byte = 3

abstract class PAPFrame : Frame() {
    override val protocol = PPP_PROTOCOL_PAP
}

class PAPAuthenticateRequest : PAPFrame() {
    override val code = PAP_CODE_AUTHENTICATE_REQUEST
    override val length: Int
        get() = headerSize + 1 + idFiled.size + 1 + passwordFiled.size

    var idFiled = ByteArray(0)
    var passwordFiled = ByteArray(0)

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        val idLength = buffer.get().toIntAsUByte()
        idFiled = ByteArray(idLength).also { buffer.get(it) }

        val passwordLength = buffer.get().toIntAsUByte()
        passwordFiled = ByteArray(passwordLength).also { buffer.get(it) }
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        buffer.put(idFiled.size.toByte())
        buffer.put(idFiled)
        buffer.put(passwordFiled.size.toByte())
        buffer.put(passwordFiled)
    }
}

abstract class PAPAuthenticateAcknowledgement : PAPFrame() {
    override val length: Int
        get() = headerSize + (if (message.isEmpty()) 0 else message.size + 1)

    private var message = ByteArray(0)

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        when (val remaining = length - headerSize) {
            0 -> {}
            in 1..Int.MAX_VALUE -> {
                val messageLength = buffer.get().toIntAsUByte()
                assertAlways(messageLength == remaining - 1)
                message = ByteArray(messageLength).also { buffer.get(it) }
            }

            else -> throw ParsingDataUnitException()
        }
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        if (message.isNotEmpty()) {
            buffer.put(message.size.toByte())
            buffer.put(message)
        }
    }
}

class PAPAuthenticateAck : PAPAuthenticateAcknowledgement() {
    override val code = PAP_CODE_AUTHENTICATE_ACK
}

class PAPAuthenticateNak : PAPAuthenticateAcknowledgement() {
    override val code = PAP_CODE_AUTHENTICATE_NAK
}
