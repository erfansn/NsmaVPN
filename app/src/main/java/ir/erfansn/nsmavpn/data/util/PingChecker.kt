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
