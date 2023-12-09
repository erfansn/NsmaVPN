package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.OscPrefKey
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ControlMessage
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Result
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Where
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.IpcpConfigureAck
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.IpcpConfigureFrame
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.IpcpConfigureReject
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.IpcpConfigureRequest
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.IpcpAddressOption
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.IpcpOptionPack
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.OPTION_TYPE_IPCP_DNS
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.OPTION_TYPE_IPCP_IP
import java.net.Inet4Address

class IpcpClient(bridge: ClientBridge) : ConfigClient<IpcpConfigureFrame>(Where.IPCP, bridge) {
    private val isDNSRequested = OscPrefKey.DNS_DO_REQUEST_ADDRESS
    private var isDNSRejected = false
    private val requestedAddress = if (OscPrefKey.PPP_DO_REQUEST_STATIC_IPv4_ADDRESS) {
        Inet4Address.getByName(OscPrefKey.PPP_STATIC_IPv4_ADDRESS).address
    } else {
        null
    }

    override fun tryCreateServerReject(request: IpcpConfigureFrame): IpcpConfigureFrame? {
        val reject = IpcpOptionPack()

        if (request.options.unknownOptions.isNotEmpty()) {
            reject.unknownOptions = request.options.unknownOptions
        }

        request.options.dnsOption?.also { // client doesn't have dns server
            reject.dnsOption = request.options.dnsOption
        }

        return if (reject.allOptions.isNotEmpty()) {
            IpcpConfigureReject().also {
                it.id = request.id
                it.options = reject
                it.options.order = request.options.order
            }
        } else null
    }

    override fun tryCreateServerNak(request: IpcpConfigureFrame): IpcpConfigureFrame? {
        return null
    }

    override fun createServerAck(request: IpcpConfigureFrame): IpcpConfigureFrame {
        return IpcpConfigureAck().also {
            it.id = request.id
            it.options = request.options
        }
    }

    override fun createClientRequest(): IpcpConfigureFrame {
        val request = IpcpConfigureRequest()

        requestedAddress?.also {
            it.copyInto(bridge.currentIPv4)
        }

        request.options.ipOption = IpcpAddressOption(OPTION_TYPE_IPCP_IP).also {
            bridge.currentIPv4.copyInto(it.address)
        }

        if (isDNSRequested && !isDNSRejected) {
            request.options.dnsOption = IpcpAddressOption(OPTION_TYPE_IPCP_DNS).also {
                bridge.currentProposedDNS.copyInto(it.address)
            }
        }

        return request
    }

    override suspend fun tryAcceptClientNak(nak: IpcpConfigureFrame) {
        nak.options.ipOption?.also {
            if (requestedAddress != null) {
                bridge.controlMailbox.send(ControlMessage(Where.IPCP, Result.ERR_ADDRESS_REJECTED))
            } else {
                it.address.copyInto(bridge.currentIPv4)
            }
        }

        nak.options.dnsOption?.also {
            it.address.copyInto(bridge.currentProposedDNS)
        }
    }

    override suspend fun tryAcceptClientReject(reject: IpcpConfigureFrame) {
        reject.options.ipOption?.also {
            bridge.controlMailbox.send(ControlMessage(Where.IPCP_IP, Result.ERR_OPTION_REJECTED))
        }

        reject.options.dnsOption?.also {
            isDNSRejected = true
        }
    }
}
