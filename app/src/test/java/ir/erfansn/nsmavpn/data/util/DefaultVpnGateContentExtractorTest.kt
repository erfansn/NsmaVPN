package ir.erfansn.nsmavpn.data.util

import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class DefaultVpnGateContentExtractorTest {

    private val vpnGateContentExtractor = DefaultVpnGateContentExtractor()
    private val classLoader = ClassLoader.getSystemClassLoader()
    private val mockWebServer = MockWebServer()

    private lateinit var mockWebServerAddress: HttpUrl

    @Before
    fun setUp() {
        mockWebServerAddress = mockWebServer.url("/test")
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun extractsVpnServersLinkCorrectly() {
        val response = classLoader.getResource("vpngate.html").readText()
        mockWebServer.enqueue(
            MockResponse()
                .setBody(response)
                .setResponseCode(200)
        )

        val servers = vpnGateContentExtractor.extractSstpVpnServers(mockWebServerAddress.toString())
        assertThat(servers.size).isEqualTo(19)
    }
}
