package ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.probeByte
import java.nio.ByteBuffer

const val OPTION_TYPE_IPCP_IP: Byte = 0x03
const val OPTION_TYPE_IPCP_DNS = 0x81.toByte()

class IpcpAddressOption(override val type: Byte) : Option() {
    val address = ByteArray(4)
    override val length = headerSize + address.size

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        buffer.get(address)
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        buffer.put(address)
    }
}

class IpcpOptionPack(givenLength: Int = 0) : OptionPack(givenLength) {
    var ipOption: IpcpAddressOption? = null
    var dnsOption: IpcpAddressOption? = null

    override val knownOptions: List<Option>
        get() = mutableListOf<Option>().also { options ->
            ipOption?.also { options.add(it) }
            dnsOption?.also { options.add(it) }
        }

    override fun retrieveOption(buffer: ByteBuffer): Option {
        val option = when (val type = buffer.probeByte(0)) {
            OPTION_TYPE_IPCP_IP -> IpcpAddressOption(type).also { ipOption = it }

            OPTION_TYPE_IPCP_DNS -> IpcpAddressOption(type).also { dnsOption = it }

            else -> UnknownOption(type)
        }

        option.read(buffer)

        return option
    }
}
