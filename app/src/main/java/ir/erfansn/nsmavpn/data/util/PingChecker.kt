package ir.erfansn.nsmavpn.data.util

import android.content.res.Resources.NotFoundException
import android.os.Build
import ir.erfansn.nsmavpn.data.util.PingChecker.Companion.NOT_AVAILABLE
import ir.erfansn.nsmavpn.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.measureTime

class DefaultPingChecker @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PingChecker {

    private val runtime = Runtime.getRuntime()

    override suspend fun measure(hostName: String) = withContext(ioDispatcher) {
        runCatching {
            if (!isEmulator) {
                val subprocess = runtime.exec("ping -c 4 $hostName")
                val output = subprocess.waitForOutput(PING_TIMEOUT_MS)
                parsePingResult(output)
            } else {
                fun URL.measureTcpPing() = measureTime {
                    openConnection().apply {
                        connectTimeout = PING_TIMEOUT_MS.toInt()
                    }.connect()
                }.toDouble(
                    unit = DurationUnit.MILLISECONDS
                )

                runCatching {
                    URL("http://$hostName").measureTcpPing()
                }.getOrElse {
                    URL("https://$hostName").measureTcpPing()
                }
            }
        }.getOrDefault(NOT_AVAILABLE)
    }

    private val isEmulator get() = Build.TAGS == "dev-keys"

    private fun parsePingResult(result: String): Double {
        val matchResult = AVERAGE_PING_PATTERN.find(result) ?: throw NotFoundException()
        return matchResult.groupValues[1].toDouble()
    }

    companion object {
        private const val PING_TIMEOUT_MS = 5 * 1000L

        private val AVERAGE_PING_PATTERN = "rtt min/avg/max/mdev = .*/(.*)/.*/.* ms".toRegex()
    }
}

interface PingChecker {
    suspend fun measure(hostName: String): Double
    suspend fun isReachable(hostName: String = "google.com"): Boolean {
        return hostName.isNotEmpty() && measure(hostName) != NOT_AVAILABLE
    }

    companion object {
        const val NOT_AVAILABLE = -1.0
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun Process.waitForOutput(millis: Long) = coroutineScope {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        launch { waitFor(millis, TimeUnit.MILLISECONDS) }.join()
    } else {
        withTimeout(millis) { launch { waitFor() }.join() }
    }

    inputStream.reader().use(InputStreamReader::readText)
}
