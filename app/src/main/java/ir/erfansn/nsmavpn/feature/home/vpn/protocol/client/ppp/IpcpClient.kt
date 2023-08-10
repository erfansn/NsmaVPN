package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp

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

class IpcpClient(bridge: ClientBridge) : ConfigClient<IpcpConfigureFrame>(Where.IPCP, bridge) {
    private var isDNSRejected = false

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

        request.options.ipOption = IpcpAddressOption(OPTION_TYPE_IPCP_IP).also {
            bridge.currentIPv4.copyInto(it.address)
        }

        if (bridge.DNS_DO_REQUEST_ADDRESS && !isDNSRejected) {
            request.options.dnsOption = IpcpAddressOption(OPTION_TYPE_IPCP_DNS).also {
                bridge.currentProposedDNS.copyInto(it.address)
            }
        }

        return request
    }

    override fun tryAcceptClientNak(nak: IpcpConfigureFrame) {
        nak.options.ipOption?.also {
            it.address.copyInto(bridge.currentIPv4)
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
