package ir.erfansn.nsmavpn.data.util

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test

// Because of problem running as local test in Windows machine
@ExperimentalCoroutinesApi
class DefaultVpnGateContentExtractorTest {

    @get:Rule
    val mockWebServer = MockWebServer()

    private val vpnGateContentExtractor = DefaultVpnGateContentExtractor()

    @Test
    fun extractsSstpVpnServersLinkCorrectly() = runTest {
        val mockWebServerAddress = mockWebServer.url("/")
        val testAppContext = InstrumentationRegistry.getInstrumentation().context
        val response = testAppContext.assets.open("vpngate_content.html").reader().use { it.readText() }
        mockWebServer.enqueue(
            MockResponse()
                .setBody(response)
                .setResponseCode(200)
        )

        val servers = vpnGateContentExtractor.extractSstpVpnServers(mockWebServerAddress.toUrl())
        assertThat(servers.size).isEqualTo(90)
        assertThat(servers.map { it.address.hostName }).containsNoneIn(notSupportedSstpServersHostname)
    }
}

private val notSupportedSstpServersHostname = listOf(
    "vpn480029601.opengw.net",
    "vpn770930829.opengw.net",
    "vpn538967253.opengw.net",
    "vpn103843903.opengw.net",
    "vpn989219124.opengw.net",
    "vpn392131446.opengw.net",
    "vpn955996296.opengw.net",
    "vpn460501145.opengw.net",
    "vpn577037995.opengw.net",
    "vpn847390971.opengw.net",
)

