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

import ir.erfansn.nsmavpn.data.source.local.datastore.Server

class MockVpnServersLocalDataSource : VpnServersLocalDataSource {

    private val allServers = mutableListOf<Server>()
    private val unavailableServers = mutableListOf<Server>()

    var deleteFromUnavailableVpnServersCallCount = 0
        private set

    override suspend fun getAvailableSstpVpnServers(): List<Server> {
        return allServers - unavailableServers.toSet()
    }

    override suspend fun getUnavailableVpnServers(): List<Server> {
        return unavailableServers
    }

    override suspend fun saveSstpVpnServers(servers: List<Server>) {
        allServers.addAll(servers)
    }

    override suspend fun saveAsUnavailableVpnServers(servers: List<Server>) {
        unavailableServers.addAll(servers)
    }

    override suspend fun deleteFromUnavailableVpnServers(servers: List<Server>) {
        unavailableServers.removeAll(servers)
        deleteFromUnavailableVpnServersCallCount++
    }
}
