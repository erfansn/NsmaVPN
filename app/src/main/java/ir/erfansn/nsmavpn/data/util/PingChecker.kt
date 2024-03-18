package ir.erfansn.nsmavpn.data.util

import ir.erfansn.nsmavpn.data.util.PingChecker.Companion.NOT_AVAILABLE
import ir.erfansn.nsmavpn.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runInterruptible
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.measureTime

interface PingChecker {
    suspend fun measure(hostName: String, port: Int = 80): Double
    suspend fun isReachable(hostName: String, port: Int = 80): Boolean =
        measure(hostName, port) != NOT_AVAILABLE

    companion object {
        const val NOT_AVAILABLE = -1.0
    }
}

class DefaultPingChecker @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PingChecker {

    override suspend fun measure(hostName: String, port: Int) =
        runCatching {
            require(hostName.isNotEmpty())

            runInterruptible(ioDispatcher) {
                val inetAddress = InetAddress.getByName(hostName)
                Socket().use {
                    measureTime {
                        it.connect(
                            InetSocketAddress(inetAddress.hostAddress, port),
                            PING_TIMEOUT_MS
                        )
                    }.toDouble(
                        unit = DurationUnit.MILLISECONDS
                    )
                }
            }
        }
        .getOrDefault(NOT_AVAILABLE)

    companion object {
        private const val PING_TIMEOUT_MS = 4 * 1000
    }
}
