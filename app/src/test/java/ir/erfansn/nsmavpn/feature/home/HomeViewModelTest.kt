package ir.erfansn.nsmavpn.feature.home

import com.google.common.truth.Truth.assertThat
import ir.erfansn.nsmavpn.data.repository.FakeLastVpnConnectionRepository
import ir.erfansn.nsmavpn.data.repository.StubProfileRepository
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.util.FakeNetworkUsageTracker
import ir.erfansn.nsmavpn.feature.home.vpn.ConnectionState
import ir.erfansn.nsmavpn.feature.home.vpn.CountryCode
import ir.erfansn.nsmavpn.feature.home.vpn.DummySstpVpnServiceAction
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnServiceState
import ir.erfansn.nsmavpn.sync.StubVpnServersSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeLastVpnConnectionRepository = FakeLastVpnConnectionRepository()
    private lateinit var homeViewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        homeViewModel = HomeViewModel(
            lastVpnConnectionRepository = fakeLastVpnConnectionRepository,
            networkUsageTracker = FakeNetworkUsageTracker,
            sstpVpnServiceAction = DummySstpVpnServiceAction,
            vpnServersSyncManager = StubVpnServersSyncManager,
            profileRepository = StubProfileRepository,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun returnsZeroAsDataTrafficUsage_whenNotConnectedYet() = runTest {
        backgroundScope.launch(testDispatcher) {
            homeViewModel.dataTraffic.collect()
        }

        assertThat(homeViewModel.dataTraffic.value).isEqualTo(DataTraffic(upload = 0, download = 0))
    }

    @Test
    fun returnsNullAsDataTrafficUsage_whenCannotToRetrieveDataTrafficUsage() = runTest {
        fakeLastVpnConnectionRepository.saveLastConnectionInfo(Server.getDefaultInstance(), -1000)
        homeViewModel.updateVpnServiceState(
            state = SstpVpnServiceState(
                connectionState = ConnectionState.Connected(
                    serverCountryCode = CountryCode("US")
                )
            )
        )

        backgroundScope.launch(testDispatcher) {
            homeViewModel.dataTraffic.collect()
        }

        assertThat(homeViewModel.dataTraffic.value).isNull()
    }
}