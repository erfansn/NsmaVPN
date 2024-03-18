package ir.erfansn.nsmavpn.data.source.local

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.google.common.truth.Truth.assertThat
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnServers
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnServersSerializer
import ir.erfansn.nsmavpn.data.source.local.datastore.copy
import ir.erfansn.nsmavpn.data.source.local.datastore.server
import ir.erfansn.nsmavpn.data.source.local.datastore.urlParts
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DefaultVpnServersLocalDataSourceTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val testDataStore: DataStore<VpnServers> = DataStoreFactory.create(
        serializer = VpnServersSerializer,
        scope = testScope,
        produceFile = { temporaryFolder.newFile("test_vpn_servers") }
    )

    private val vpnServersLocalDataStore = DefaultVpnServersLocalDataSource(
        dataStore = testDataStore,
        defaultDispatcher = testDispatcher,
    )

    @Test
    fun deletesSpecifiedUnavailableServers_whenPassedToIt() = testScope.runTest {
        val servers = fakeServers
        testDataStore.updateData {
            it.copy {
                unavailableServers += servers
            }
        }

        vpnServersLocalDataStore.deleteFromUnavailableVpnServers(servers.drop(1))

        val vpnServers = testDataStore.data.first()
        assertThat(vpnServers.unavailableServersList).containsExactly(servers.first())
    }

    @Test
    fun updatesServerCountryCode_whenPassedServerIsDuplicateWithDifferentCountryCode() = testScope.runTest {
        val server = fakeServers.first()
        testDataStore.updateData {
            it.copy {
                sstpServers += server
            }
        }

        val ccChangedServer = server.copy { countryCode = "34" }
        vpnServersLocalDataStore.saveSstpVpnServers(listOf(ccChangedServer))

        val vpnServers = testDataStore.data.first()
        assertThat(vpnServers.sstpServersList).containsExactly(ccChangedServer)
    }

    @Test
    fun getsEmptyListOfAvailableSstpServers_whenAllServersAreUnavailable() = testScope.runTest {
        val servers = fakeServers
        testDataStore.updateData {
            it.copy {
                sstpServers += servers
                unavailableServers += servers
            }
        }

        val availableServers = vpnServersLocalDataStore.getAvailableSstpVpnServers()

        assertThat(availableServers).isEmpty()
    }

    @Test
    fun getsOnlyAvailableSstpServers_whenRequested() = testScope.runTest {
        val servers = fakeServers
        testDataStore.updateData {
            it.copy {
                sstpServers += servers
                unavailableServers += servers.take(2)
            }
        }

        val availableServers = vpnServersLocalDataStore.getAvailableSstpVpnServers()

        assertThat(availableServers).isEqualTo(servers.drop(2))
    }

    @Test(expected = IllegalStateException::class)
    fun throwsException_whenRequestedToGetUnavailableServersThatDoesNotExist() = testScope.runTest {
        val servers = fakeServers
        testDataStore.updateData {
            it.copy {
                unavailableServers += servers
            }
        }

        vpnServersLocalDataStore.getUnavailableVpnServers()
    }

    @Test
    fun getsUnavailableServers_whenRequested() = testScope.runTest {
        val servers = fakeServers
        testDataStore.updateData {
            it.copy {
                sstpServers += servers
                unavailableServers += servers.take(2)
            }
        }

        val unavailableServers = vpnServersLocalDataStore.getUnavailableVpnServers()

        assertThat(unavailableServers).isEqualTo(servers.take(2))
    }
}

private val fakeServers = List(5) {
    server {
        countryCode = "$it"
        address = urlParts {
            hostName = "nsmavpn$it.vpngate.net"
            portNumber = 443
        }
    }
}
