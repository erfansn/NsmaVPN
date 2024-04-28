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
package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ppp

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.DEFAULT_MRU
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.MIN_MRU
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ControlMessage
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Result
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Where
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPConfigureAck
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPConfigureFrame
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPConfigureNak
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPConfigureReject
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.LCPConfigureRequest
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.AuthOptionMSChapv2
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.AuthOptionPAP
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.AuthOptionUnknown
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.LCPOptionPack
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.MRUOption
import kotlin.math.max
import kotlin.math.min

class LcpClient(bridge: ClientBridge) : ConfigClient<LCPConfigureFrame>(Where.LCP, bridge) {
    private var isMruRejected = false

    override fun tryCreateServerReject(request: LCPConfigureFrame): LCPConfigureFrame? {
        val reject = LCPOptionPack()

        if (request.options.unknownOptions.isNotEmpty()) {
            reject.unknownOptions = request.options.unknownOptions
        }

        return if (reject.allOptions.isNotEmpty()) {
            LCPConfigureReject().also {
                it.id = request.id
                it.options = reject
                it.options.order = request.options.order
            }
        } else null
    }

    override fun tryCreateServerNak(request: LCPConfigureFrame): LCPConfigureFrame? {
        val nak = LCPOptionPack()

        val serverMru = request.options.mruOption?.unitSize ?: DEFAULT_MRU
        if (serverMru < bridge.PPP_MTU) {
            nak.mruOption = MRUOption().also { it.unitSize = bridge.PPP_MTU }
        }

        val serverAuth = request.options.authOption ?: AuthOptionUnknown(0)
        var isAuthAcknowledged = false

        when (serverAuth) {
            is AuthOptionMSChapv2 -> {
                if (bridge.PPP_MSCHAPv2_ENABLED) {
                    bridge.currentAuth = serverAuth
                    isAuthAcknowledged = true
                }
            }

            is AuthOptionPAP -> {
                if (bridge.PPP_PAP_ENABLED) {
                    bridge.currentAuth = serverAuth
                    isAuthAcknowledged = true
                }
            }
        }

        if (!isAuthAcknowledged) {
            nak.authOption = bridge.getPreferredAuthOption()
        }


        return if (nak.allOptions.isNotEmpty()) {
            LCPConfigureNak().also {
                it.id = request.id
                it.options = nak
                it.options.order = request.options.order
            }
        } else null
    }

    override fun createServerAck(request: LCPConfigureFrame): LCPConfigureFrame {
        return LCPConfigureAck().also {
            it.id = request.id
            it.options = request.options
        }
    }


    override fun createClientRequest(): LCPConfigureFrame {
        val request = LCPConfigureRequest()

        if (!isMruRejected) {
            request.options.mruOption = MRUOption().also { it.unitSize = bridge.currentMRU }
        }

        return request
    }

    override suspend fun tryAcceptClientNak(nak: LCPConfigureFrame) {
        nak.options.mruOption?.also {
            bridge.currentMRU = max(min(it.unitSize, bridge.PPP_MRU), MIN_MRU)
        }
    }

    override suspend fun tryAcceptClientReject(reject: LCPConfigureFrame) {
        reject.options.mruOption?.also {
            isMruRejected = true

            if (DEFAULT_MRU > bridge.PPP_MRU) {
                bridge.controlMailbox.send(
                    ControlMessage(Where.LCP_MRU, Result.ERR_OPTION_REJECTED)
                )
            }
        }

        reject.options.authOption?.also {
            bridge.controlMailbox.send(ControlMessage(Where.LCP_AUTH, Result.ERR_OPTION_REJECTED))
        }
    }
}
