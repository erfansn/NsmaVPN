package ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.Ipv6cpOptionPack
import java.nio.ByteBuffer

abstract class Ipv6cpFrame : Frame() {
    override val protocol = PPP_PROTOCOL_IPv6CP
}

abstract class Ipv6cpConfigureFrame : Ipv6cpFrame() {
    override val length: Int
        get() = headerSize + options.length

    var options: Ipv6cpOptionPack = Ipv6cpOptionPack()

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        options = Ipv6cpOptionPack(givenLength - length).also {
            it.read(buffer)
        }
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        options.write(buffer)
    }
}

class Ipv6cpConfigureRequest : Ipv6cpConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_REQUEST
}

class Ipv6cpConfigureAck : Ipv6cpConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_ACK
}

class Ipv6cpConfigureNak : Ipv6cpConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_NAK
}

class Ipv6cpConfigureReject : Ipv6cpConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_REJECT
}
