@file:OptIn(ExperimentalCoroutinesApi::class)

package ir.erfansn.nsmavpn.data.source.local

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnProviderSerializer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultVpnProviderLocalDataSourceTest {

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testCoroutineDispatcher = UnconfinedTestDispatcher()
    private val testCoroutineScope = TestScope(testCoroutineDispatcher)
    private val testDataStore = DataStoreFactory.create(
        serializer = VpnProviderSerializer,
        scope = testCoroutineScope,
        produceFile = { testContext.dataStoreFile("test") }
    )
    private val dataSource = DefaultVpnProviderLocalDataSource(testDataStore)

    @Test
    fun savesVpnServersCorrectly() = testCoroutineScope.runTest {
        dataSource.saveVpnServers(fakeServers)

        val vpnProvider = testDataStore.data.first()
        assertThat(vpnProvider.serversList.size).isEqualTo(2)
        assertThat(vpnProvider.serversList).isEqualTo(fakeServers)
    }

    @Test
    fun ignoresSavingDuplicateVpnServers() = testCoroutineScope.runTest {
        dataSource.saveVpnServers(fakeServers)
        dataSource.saveVpnServers(fakeOtherServers)

        val vpnProvider = testDataStore.data.first()
        assertThat(vpnProvider.serversList.size).isEqualTo(3)
        assertThat(vpnProvider.serversList).isEqualTo(fakeOtherServers)
    }
}

val fakeServers = listOf(
    Server.newBuilder()
        .setCountry("japan")
        .setHostName("public-vpn-200.opengw.net")
        .setPort("443")
        .build(),
    Server.newBuilder()
        .setCountry("japan")
        .setHostName("public-vpn-220.opengw.net")
        .setPort("1345")
        .build(),
)

val fakeOtherServers = listOf(
    Server.newBuilder()
        .setCountry("usa")
        .setHostName("vpn83734893.opengw.net")
        .setPort("1880")
        .build(),
    *fakeServers.toTypedArray()
)
