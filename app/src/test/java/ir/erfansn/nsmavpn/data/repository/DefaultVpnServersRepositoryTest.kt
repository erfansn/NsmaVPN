package ir.erfansn.nsmavpn.data.repository

import com.google.common.truth.Truth.assertThat
import ir.erfansn.nsmavpn.data.source.local.FakeLastVpnConnectionLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.MockVpnServersLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.LastVpnConnectionLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.VpnServersLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.server
import ir.erfansn.nsmavpn.data.source.local.datastore.urlParts
import ir.erfansn.nsmavpn.data.util.PingChecker
import ir.erfansn.nsmavpn.data.util.StubPingChecker
import ir.erfansn.nsmavpn.data.util.StubVpnGateContentExtractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.URL
import kotlin.random.Random

class DefaultVpnServersRepositoryTest {

    private val stubPingChecker = StubPingChecker()
    private val stubVpnGateMailRepository = StubVpnGateMailRepository()
    private val stubVpnGateContentExtractor = StubVpnGateContentExtractor()

    private lateinit var mockVpnServersLocalDataSource: MockVpnServersLocalDataSource
    private lateinit var fakeLastVpnConnectionLocalDataSource: LastVpnConnectionLocalDataSource
    private lateinit var defaultVpnServersRepository: DefaultVpnServersRepository

    @Before
    fun setUp() {
        mockVpnServersLocalDataSource = MockVpnServersLocalDataSource()
        fakeLastVpnConnectionLocalDataSource = FakeLastVpnConnectionLocalDataSource()
        defaultVpnServersRepository = DefaultVpnServersRepository(
            profileRepository = StubProfileRepository,
            vpnGateMailRepository = stubVpnGateMailRepository,
            vpnServersLocalDataSource = mockVpnServersLocalDataSource,
            lastVpnConnectionLocalDataSource = fakeLastVpnConnectionLocalDataSource,
            vpnGateContentExtractor = stubVpnGateContentExtractor,
            pingChecker = stubPingChecker
        )
    }

    @After
    fun tearDown() {
        stubPingChecker.measureResult = emptyMap()
        stubVpnGateMailRepository.mirrorLinks = emptyList()
    }

    @Test(expected = NoAvailableVpnServerException::class)
    fun throwsException_whenTryToFindFastestServerOnEmptyList() = runTest {
        defaultVpnServersRepository.findFastestSstpVpnServer()
    }

    @Test(expected = NoAvailableVpnServerException::class)
    fun throwsException_whenFindingFastestServerOnUnreachableServers() = runTest {
        val servers = generateUniqueServers(5)
        stubPingChecker.measureResult = servers with List(servers.size) { PingChecker.NOT_AVAILABLE }
        mockVpnServersLocalDataSource.saveSstpVpnServers(servers)

        defaultVpnServersRepository.findFastestSstpVpnServer()
    }

    @Test
    fun returnsServerWithMinPing_whenFindingFastestServer() = runTest {
        val servers = generateUniqueServers(5)
        stubPingChecker.measureResult = servers with listOf(50.0, 10.0, 2.0, PingChecker.NOT_AVAILABLE, 20.0)
        mockVpnServersLocalDataSource.saveSstpVpnServers(servers)

        val result = defaultVpnServersRepository.findFastestSstpVpnServer()

        assertThat(result).isEqualTo(servers[2])
    }

    @Test
    fun returnsNullAsLastVpnConnectionServer_ifItWasBlacklisted() = runTest {
        val server = randomServer()
        fakeLastVpnConnectionLocalDataSource.saveLastConnectionInfo(server, Random.nextLong())
        mockVpnServersLocalDataSource.saveAsUnavailableVpnServers(listOf(server))

        val result = defaultVpnServersRepository.tryToGetLastVpnConnectionServer()

        assertThat(result).isNull()
    }

    @Test
    fun returnsNullAsLastVpnConnectionServer_ifItDoesNotReachable() = runTest {
        val server = randomServer()
        stubPingChecker.measureResult = listOf(server) with listOf(PingChecker.NOT_AVAILABLE)
        fakeLastVpnConnectionLocalDataSource.saveLastConnectionInfo(server, Random.nextLong())

        val result = defaultVpnServersRepository.tryToGetLastVpnConnectionServer()

        assertThat(result).isNull()
    }

    @Test
    fun returnsLastVpnConnectionServer_whenItIsNotInBlacklistAndIsReachable() = runTest {
        val server = randomServer()
        stubPingChecker.measureResult = listOf(server) with listOf(12.0)
        fakeLastVpnConnectionLocalDataSource.saveLastConnectionInfo(server, Random.nextLong())

        val result = defaultVpnServersRepository.tryToGetLastVpnConnectionServer()

        assertThat(result).isEqualTo(server)
    }

    @Test(expected = NotFoundServerException::class)
    fun throwsException_whenThereIsNoAnyMirrorLinkToExtractAndCollectServers() = runTest {
        stubVpnGateMailRepository.mirrorLinks = emptyList()

        defaultVpnServersRepository.collectVpnServers()
    }

    @Test
    fun saveObtainedServers_whenCollectingThem() = runTest {
        val servers = generateUniqueServers(2)
        stubVpnGateMailRepository.mirrorLinks = listOf(URL("https://fake.com"))
        stubVpnGateContentExtractor.sstpServers = servers

        defaultVpnServersRepository.collectVpnServers()

        val savedServers = mockVpnServersLocalDataSource.getAvailableSstpVpnServers()
        assertThat(savedServers).isEqualTo(servers)
    }
    
    @Test
    fun doesNothing_whenNoAnyAvailableServerBecomeUnreachable() = runTest {
        val unavailableServers = mockVpnServersLocalDataSource.getUnavailableVpnServers()

        defaultVpnServersRepository.blockUnreachableVpnServers()

        val currentUnavailableServers = mockVpnServersLocalDataSource.getUnavailableVpnServers()
        assertThat(currentUnavailableServers).isEqualTo(unavailableServers)
    }

    @Test
    fun markServersAsUnavailable_whenSomeAvailableServersBecomeUnreachable() = runTest {
        val servers = generateUniqueServers(4)
        mockVpnServersLocalDataSource.saveSstpVpnServers(servers)
        val pingResult = listOf(29.0, 50.0, PingChecker.NOT_AVAILABLE, PingChecker.NOT_AVAILABLE)
        stubPingChecker.measureResult = servers with pingResult

        defaultVpnServersRepository.blockUnreachableVpnServers()

        val unavailableServers = mockVpnServersLocalDataSource.getUnavailableVpnServers()
        assertThat(servers.takeLast(2)).isEqualTo(unavailableServers)
    }

    @Test
    fun deletesReachableServersOnlyOnce_whenTryToCallItFromTwoDifferencePlace() = runTest {
        val servers = generateUniqueServers(4)
        mockVpnServersLocalDataSource.saveAsUnavailableVpnServers(servers)

        coroutineScope {
            launch {
                defaultVpnServersRepository.unblockReachableVpnServers()
            }
            launch {
                defaultVpnServersRepository.unblockReachableVpnServers()
            }
        }

        val callCount = mockVpnServersLocalDataSource.deleteFromUnavailableVpnServersCallCount
        assertThat(callCount).isEqualTo(1)
    }
}

private infix fun <T, R> List<T>.with(other: List<R>): Map<T, R> = zip(other).toMap()

fun generateUniqueServers(count: Int): List<Server> {
    return buildSet<Server> {
            var n = 0
            // TODO: Should report this issue to Kotlin team; Type inference doesn't work with while loop
            while (n < count) {
                this += randomServer()
                if (size == n + 1) n++
            }
        }
        .toList()
}

private fun randomServer() = server {
    countryCode = Random.nextInt(10, 100).toString()
    address = urlParts {
        hostName = "sever" + Random.nextInt() + ".com"
        portNumber = Random.nextInt(1, 65535)
    }
}
