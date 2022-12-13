@file:OptIn(ExperimentalCoroutinesApi::class)

package ir.erfansn.nsmavpn.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnProvider
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnProviderSerializer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DefaultVpnProviderLocalDataSourceTest {

    private val testCoroutineDispatcher = UnconfinedTestDispatcher()
    private val testCoroutineScope = TestScope(testCoroutineDispatcher)

    private lateinit var testDataStoreFile: File
    private lateinit var testDataStore: DataStore<VpnProvider>
    private lateinit var vpnProviderLocalDataSource: VpnProviderLocalDataSource

    @Before
    fun setUp() {
        val testContext: Context = ApplicationProvider.getApplicationContext()
        testDataStoreFile = testContext.dataStoreFile("test")
        testDataStore = DataStoreFactory.create(
            serializer = VpnProviderSerializer,
            scope = testCoroutineScope,
            produceFile = { testDataStoreFile }
        )
        vpnProviderLocalDataSource = DefaultVpnProviderLocalDataSource(testDataStore)
    }

    @After
    fun tearDown() {
        testDataStoreFile.delete()
    }

    @Test
    fun savesVpnServersCorrectly() = testCoroutineScope.runTest {
        vpnProviderLocalDataSource.saveVpnServers(fakeServersOne)

        val vpnServers = vpnProviderLocalDataSource.getVpnServers()
        assertThat(vpnServers.size).isEqualTo(2)
        assertThat(vpnServers).isEqualTo(fakeServersOne)
    }

    @Test
    fun ignoresSavingDuplicateVpnServers() = testCoroutineScope.runTest {
        vpnProviderLocalDataSource.saveVpnServers(fakeServersOne)
        vpnProviderLocalDataSource.saveVpnServers(fakeServersTwo)

        val vpnServers = vpnProviderLocalDataSource.getVpnServers()
        assertThat(vpnServers.size).isEqualTo(3)
        assertThat(vpnServers).isEqualTo(fakeServersTwo)
    }
}

val fakeServersOne = listOf(
    Server.newBuilder()
        .setCountry("japan")
        .setHostName("public-vpn-200.opengw.net")
        .setPort(433)
        .build(),
    Server.newBuilder()
        .setCountry("japan")
        .setHostName("public-vpn-220.opengw.net")
        .setPort(1345)
        .build(),
)

val fakeServersTwo = listOf(
    *fakeServersOne.toTypedArray(),
    Server.newBuilder()
        .setCountry("usa")
        .setHostName("vpn83734893.opengw.net")
        .setPort(1880)
        .build(),
)
