package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client

import ir.erfansn.nsmavpn.feature.home.vpn.service.SstpVpnService
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._DNS_DO_REQUEST_ADDRESS
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._DNS_DO_USE_CUSTOM_SERVER
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._HOME_HOSTNAME
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._HOME_PASSWORD
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._HOME_USERNAME
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._PPP_IPv4_ENABLED
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._PPP_IPv6_ENABLED
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._PPP_MRU
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._PPP_MSCHAPv2_ENABLED
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._PPP_MTU
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._PPP_PAP_ENABLED
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._ROUTE_DO_ENABLE_APP_BASED_RULE
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.terminal.IpTerminal
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.terminal.SSLTerminal
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.AuthOption
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.AuthOptionMSChapv2
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.AuthOptionPAP
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class ChapMessage {
    val serverChallenge = ByteArray(16)
    val clientChallenge = ByteArray(16)
    val serverResponse = ByteArray(42)
    val clientResponse = ByteArray(24)
}

enum class Where {
    SSL,
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

    val HOME_HOSTNAME = _HOME_HOSTNAME
    val HOME_USERNAME = _HOME_USERNAME
    val HOME_PASSWORD = _HOME_PASSWORD
    val PPP_MRU = _PPP_MRU
    val PPP_MTU = _PPP_MTU
    val PPP_PAP_ENABLED = _PPP_PAP_ENABLED
    val PPP_MSCHAPv2_ENABLED = _PPP_MSCHAPv2_ENABLED
    val PPP_IPv4_ENABLED = _PPP_IPv4_ENABLED
    val PPP_IPv6_ENABLED = _PPP_IPv6_ENABLED
    val DNS_DO_REQUEST_ADDRESS = _DNS_DO_REQUEST_ADDRESS
    val DNS_DO_USE_CUSTOM_SERVER = _DNS_DO_USE_CUSTOM_SERVER
    val ROUTE_DO_ENABLE_APP_BASED_RULE = _ROUTE_DO_ENABLE_APP_BASED_RULE

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
