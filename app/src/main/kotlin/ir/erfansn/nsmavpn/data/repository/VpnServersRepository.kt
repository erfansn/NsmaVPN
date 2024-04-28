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
package ir.erfansn.nsmavpn.data.repository

import io.github.reactivecircus.cache4k.Cache
import ir.erfansn.nsmavpn.data.source.local.LastVpnConnectionLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.VpnServersLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.LastVpnConnection
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.util.PingChecker
import ir.erfansn.nsmavpn.data.util.PingChecker.Companion.NOT_AVAILABLE
import ir.erfansn.nsmavpn.data.util.VpnGateContentExtractor
import ir.erfansn.nsmavpn.data.util.asyncFilter
import ir.erfansn.nsmavpn.data.util.asyncMinByOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

interface VpnServersRepository {
    suspend fun findFastestSstpVpnServer(): Server
    suspend fun tryToGetLastVpnConnectionServer(): Server?
    suspend fun collectVpnServers()
    suspend fun blockVpnServer(server: Server)
    suspend fun blockUnreachableVpnServers()
    suspend fun unblockReachableVpnServers()
}

class DefaultVpnServersRepository @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val vpnGateMailRepository: VpnGateMailRepository,
    private val vpnServersLocalDataSource: VpnServersLocalDataSource,
    private val lastVpnConnectionLocalDataSource: LastVpnConnectionLocalDataSource,
    private val vpnGateContentExtractor: VpnGateContentExtractor,
    private val pingChecker: PingChecker,
) : VpnServersRepository {

    private val timedCache = Cache.Builder<Server, Double>()
        .expireAfterAccess(15.minutes)
        .build()

    override suspend fun findFastestSstpVpnServer(): Server {
        return vpnServersLocalDataSource
            .getAvailableSstpVpnServers()
            .asyncMinByOrNull {
                timedCache.get(it) {
                    pingChecker.measure(
                        hostName = it.address.hostName,
                        port = it.address.portNumber
                    )
                }.let { result ->
                    if (result == NOT_AVAILABLE) {
                        timedCache.invalidate(it)
                        Double.MAX_VALUE
                    } else {
                        result
                    }
                }
            }?.takeIf {
                // Ensure only take the available server
                timedCache.get(it) != null
            } ?: throw NoAvailableVpnServerException()
    }

    override suspend fun tryToGetLastVpnConnectionServer(): Server? {
        val lastVpnConnection = lastVpnConnectionLocalDataSource.lastVpnConnection.first()
        return if (lastVpnConnection.isServerReusable()) lastVpnConnection.server else null
    }

    private suspend fun LastVpnConnection.isServerReusable(): Boolean {
        return server !in vpnServersLocalDataSource.getUnavailableVpnServers() &&
                pingChecker.isReachable(server.address.hostName, server.address.portNumber)
    }

    override suspend fun collectVpnServers() {
        val servers = obtainSstpVpnServers()
        vpnServersLocalDataSource.saveSstpVpnServers(servers)
    }

    private suspend fun obtainSstpVpnServers(): List<Server> {
        val emailAddress = userProfileRepository.userProfile.first().emailAddress

        tailrec suspend fun obtainServersFromVpnGate(count: Int = 0): List<Server> {
            val servers = vpnGateMailRepository.obtainMirrorSiteAddresses(
                emailAddress = emailAddress,
                lastDay = count,
            ).firstNotNullOfOrNull { link ->
                runCatching { vpnGateContentExtractor.extractSstpVpnServers(link) }.getOrNull()
            }
            return servers ?: return obtainServersFromVpnGate(count = count + 1)
        }
        return try {
            obtainServersFromVpnGate()
        } catch (e: NoVpnGateSubscriptionException) {
            throw NotFoundServerException()
        }
    }

    override suspend fun blockVpnServer(server: Server) {
        vpnServersLocalDataSource.saveAsUnavailableVpnServers(listOf(server))
    }

    override suspend fun blockUnreachableVpnServers() {
        val unreachableVpnServers = vpnServersLocalDataSource
            .getAvailableSstpVpnServers()
            .asyncFilter {
                !pingChecker.isReachable(it.address.hostName, it.address.portNumber)
            }

        vpnServersLocalDataSource.saveAsUnavailableVpnServers(unreachableVpnServers)
    }

    override suspend fun unblockReachableVpnServers() {
        if (!mutex.tryLock()) return
        val reachableServers = vpnServersLocalDataSource
            .getUnavailableVpnServers()
            .asyncFilter {
                pingChecker.isReachable(it.address.hostName, it.address.portNumber)
            }

        vpnServersLocalDataSource.deleteFromUnavailableVpnServers(reachableServers)
        mutex.unlock()
    }

    companion object {
        private val mutex = Mutex()
    }
}

class NotFoundServerException : RuntimeException()

class NoAvailableVpnServerException : Exception("Available servers list is empty")
