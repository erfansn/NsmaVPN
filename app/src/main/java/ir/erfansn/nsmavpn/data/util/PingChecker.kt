package ir.erfansn.nsmavpn.data.util

import ir.erfansn.nsmavpn.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultPingChecker @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PingChecker {

    private val runtime = Runtime.getRuntime()

    override suspend fun measure(hostName: String) = withContext(ioDispatcher) {
        val subprocess = runtime.exec("ping -c 4 $hostName")
        subprocess.waitFor()
        subprocess.inputStream.reader().use {
            parsePingResult(it.readText())
        }
    }

    override suspend fun isReachable(hostName: String): Boolean {
        return hostName.isNotEmpty() && !measure(hostName).isNaN()
    }

    private fun parsePingResult(result: String): Double {
        val matchResult = AVERAGE_PING_PATTERN.find(result)
        return matchResult?.groupValues?.get(1)?.toDouble() ?: Double.NaN
    }

    companion object {
        private val AVERAGE_PING_PATTERN = """rtt min/avg/max/mdev = .*/(.*)/.*/.* ms""".toRegex()
    }
}

interface PingChecker {
    suspend fun measure(hostName: String): Double
    suspend fun isReachable(hostName: String = "vpngate.net"): Boolean
}