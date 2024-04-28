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
package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client

import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnService
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.OscPrefKey
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.terminal.IpTerminal
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.terminal.SSLTerminal
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.AuthOption
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.AuthOptionMSChapv2
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.AuthOptionPAP
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class ChapMessage {
    val serverChallenge = ByteArray(16)
    val clientChallenge = ByteArray(16)
    val serverResponse = ByteArray(42)
    val clientResponse = ByteArray(24)
}

enum class Where {
    SSL,
    PROXY,
    SSTP_DATA,
    SSTP_CONTROL,
    SSTP_REQUEST,
    SSTP_HASH,
    PPP,
    PAP,
    CHAP,
    LCP,
    LCP_MRU,
    LCP_AUTH,
    IPCP,
    IPCP_IP,
    IPV6CP,
    IPV6CP_IDENTIFIER,
    IP,
    IPv4,
    IPv6,
    ROUTE,
    INCOMING,
    OUTGOING,
}

data class ControlMessage(
    val from: Where,
    val result: Result
)
enum class Result {
    PROCEEDED,

    // common errors
    ERR_TIMEOUT,
    ERR_COUNT_EXHAUSTED,
    ERR_UNKNOWN_TYPE, // the data cannot be parsed
    ERR_UNEXPECTED_MESSAGE, // the data can be parsed, but it's received in the wrong time
    ERR_PARSING_FAILED,
    ERR_VERIFICATION_FAILED,

    // for SSTP
    ERR_NEGATIVE_ACKNOWLEDGED,
    ERR_ABORT_REQUESTED,
    ERR_DISCONNECT_REQUESTED,

    // for PPP
    ERR_TERMINATE_REQUESTED,
    ERR_PROTOCOL_REJECTED,
    ERR_CODE_REJECTED,
    ERR_AUTHENTICATION_FAILED,
    ERR_ADDRESS_REJECTED,
    ERR_OPTION_REJECTED,

    // for IP
    ERR_INVALID_ADDRESS,

    // for INCOMING
    ERR_INVALID_PACKET_SIZE,
}

class ClientBridge(val service: SstpVpnService) {
    val builder = service.Builder()
    lateinit var handler: CoroutineExceptionHandler

    val controlMailbox = Channel<ControlMessage>(Channel.BUFFERED)

    var sslTerminal: SSLTerminal? = null
    var ipTerminal: IpTerminal? = null

    val HOME_USERNAME = OscPrefKey.HOME_USERNAME
    val HOME_PASSWORD = OscPrefKey.HOME_PASSWORD
    val PPP_MRU = OscPrefKey.PPP_MRU
    val PPP_MTU = OscPrefKey.PPP_MTU
    val PPP_PAP_ENABLED = OscPrefKey.PPP_PAP_ENABLED
    val PPP_MSCHAPv2_ENABLED = OscPrefKey.PPP_MSCHAPv2_ENABLED
    val PPP_IPv4_ENABLED = OscPrefKey.PPP_IPv4_ENABLED
    val PPP_IPv6_ENABLED = OscPrefKey.PPP_IPv6_ENABLED

    lateinit var chapMessage: ChapMessage
    val nonce = ByteArray(32)
    val guid = UUID.randomUUID().toString()
    var hashProtocol: Byte = 0

    private val mutex = Mutex()
    private var frameID = -1

    var currentMRU = PPP_MRU
    var currentAuth = getPreferredAuthOption()
    val currentIPv4 = ByteArray(4)
    val currentIPv6 = ByteArray(8)
    val currentProposedDNS = ByteArray(4)

    val disallowedApps: List<String> = OscPrefKey.DISALLOWED_APPS

    fun getPreferredAuthOption(): AuthOption {
        return if (PPP_MSCHAPv2_ENABLED) AuthOptionMSChapv2() else AuthOptionPAP()
    }

    fun attachSSLTerminal() {
        sslTerminal = SSLTerminal(this)
    }

    fun attachIPTerminal() {
        ipTerminal = IpTerminal(this)
    }

    suspend fun allocateNewFrameID(): Byte {
        mutex.withLock {
            frameID += 1
            return frameID.toByte()
        }
    }
}
