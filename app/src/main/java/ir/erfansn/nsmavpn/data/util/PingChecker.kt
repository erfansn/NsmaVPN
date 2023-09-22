package ir.erfansn.nsmavpn.data.util

import android.os.Build
import ir.erfansn.nsmavpn.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DefaultPingChecker @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PingChecker {

    private val runtime = Runtime.getRuntime()

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun measure(hostName: String) = withContext(ioDispatcher) {
        val subprocess = runtime.exec("ping -c 4 $hostName")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            subprocess.waitFor(PING_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        } else {
            withTimeout(PING_TIMEOUT_MS) { subprocess.waitFor() }
        }
        subprocess.inputStream.reader().use {
            parsePingResult(it.readText().also(::println))
        }
    }

    override suspend fun isReachable(hostName: String): Boolean {
        return hostName.isNotEmpty() && measure(hostName) != NOT_AVAILABLE
    }

    private fun parsePingResult(result: String): Double {
        val matchResult = AVERAGE_PING_PATTERN.find(result)
        return matchResult?.groupValues?.get(1)?.toDouble() ?: NOT_AVAILABLE
    }

    companion object {
        private const val NOT_AVAILABLE = -1.0
        private const val PING_TIMEOUT_MS = 20 * 1000L

        private val AVERAGE_PING_PATTERN = """rtt min/avg/max/mdev = .*/(.*)/.*/.* ms""".toRegex()
    }
}

interface PingChecker {
    suspend fun measure(hostName: String): Double
    suspend fun isReachable(hostName: String = "vpngate.net"): Boolean
}
