package ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.IpcpOptionPack
import java.nio.ByteBuffer

abstract class IpcpFrame : Frame() {
    override val protocol = PPP_PROTOCOL_IPCP
}

abstract class IpcpConfigureFrame : IpcpFrame() {
    override val length: Int
        get() = headerSize + options.length

    var options: IpcpOptionPack = IpcpOptionPack()

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        options = IpcpOptionPack(givenLength - length).also {
            it.read(buffer)
        }
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        options.write(buffer)
    }
}

class IpcpConfigureRequest : IpcpConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_REQUEST
}

class IpcpConfigureAck : IpcpConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_ACK
}

class IpcpConfigureNak : IpcpConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_NAK
}

class IpcpConfigureReject : IpcpConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_REJECT
}
