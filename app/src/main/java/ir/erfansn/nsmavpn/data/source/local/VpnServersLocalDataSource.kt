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
package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnServers
import ir.erfansn.nsmavpn.data.source.local.datastore.copy
import ir.erfansn.nsmavpn.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface VpnServersLocalDataSource {
    suspend fun getAvailableSstpVpnServers(): List<Server>
    suspend fun getUnavailableVpnServers(): List<Server>
    suspend fun saveSstpVpnServers(servers: List<Server>)
    suspend fun saveAsUnavailableVpnServers(servers: List<Server>)
    suspend fun deleteFromUnavailableVpnServers(servers: List<Server>)
}

class DefaultVpnServersLocalDataSource @Inject constructor(
    private val dataStore: DataStore<VpnServers>,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : VpnServersLocalDataSource {

    private val vpnServers: Flow<VpnServers> = dataStore.data

    override suspend fun getAvailableSstpVpnServers(): List<Server> = withContext(defaultDispatcher) {
        with(vpnServers.first()) {
            sstpServersList - unavailableServersList.toSet()
        }
    }

    override suspend fun getUnavailableVpnServers(): List<Server> =
        vpnServers.first().unavailableServersList

    override suspend fun saveSstpVpnServers(servers: List<Server>) {
        dataStore.updateData {
            it.copy {
                with(sstpServers) {
                    val newServersAddresses = servers.map { server -> server.address }
                    val previousServers = filter { server -> server.address !in newServersAddresses }
                    clear()
                    addAll(servers + previousServers)
                }
            }
        }
    }

    override suspend fun saveAsUnavailableVpnServers(servers: List<Server>) {
        dataStore.updateData {
            it.copy {
                unavailableServers += servers
            }
        }
    }

    override suspend fun deleteFromUnavailableVpnServers(servers: List<Server>) {
        dataStore.updateData {
            it.copy {
                with(unavailableServers) {
                    val reducedServers = filter { server -> server !in servers }
                    clear()
                    addAll(reducedServers)
                }
            }
        }
    }
}
