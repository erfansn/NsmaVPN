package ir.erfansn.nsmavpn.data.util

import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test

class DefaultVpnGateContentExtractorTest {

    private val classLoader = ClassLoader.getSystemClassLoader()
    private val vpnGateContentExtractor = DefaultVpnGateContentExtractor()

    @Test
    fun extractsVpnServersLinkCorrectly() {
        val response = classLoader.getResource("vpngate.html").readText()

        val mockWebServer = MockWebServer()
        val mockWebServerAddress = mockWebServer.url("/test")
        mockWebServer.enqueue(
            MockResponse()
                .setBody(response)
                .setResponseCode(200)
        )

        val servers = vpnGateContentExtractor.extractSstpVpnServers(mockWebServerAddress.toString())
        assertThat(servers.size).isEqualTo(19)

        mockWebServer.shutdown()
    }

    @Test
    fun findsVpnGateMirrorLinksCorrectly() {
        val mirrorLinks = vpnGateContentExtractor.findVpnGateMirrorLinks(fakeVpnGateEmailContent)
        assertThat(mirrorLinks).isEqualTo(
            listOf(
                "http://202.5.221.90:19927",
                "http://103.201.129.226:14684",
                "http://202.5.221.66:60279",
                "http://222.255.11.117:54621",
                "http://138.199.46.86:36667",
            )
        )
    }
}

val fakeVpnGateEmailContent = """
    Hi erfannj.en@gmail.com,

    Today's VPN Gate web site's daily mirror URLs are following.

    1. http://202.5.221.90:19927/
       (Location: Japan)

    2. http://103.201.129.226:14684/
       (Location: Japan)

    3. http://202.5.221.66:60279/
       (Location: Japan)

    4. http://222.255.11.117:54621/
       (Location: Viet Nam)

    5. http://138.199.46.86:36667/
       (Location: United Kingdom)


    *** Current Status Report ***

    VPN Gate has 13,855,295,425 cumulative VPN connections from 236 different countries, from 2013/03/08 to 2022/11/22.
    Total transferred traffic through VPN is 527,681,813.67 GB.

    Thank you for using VPN Gate all over the World !


    --- VPN Gate User Countries Realtime Top 10 Ranking ---
    No. 1: Korea Republic of (393,683,342 cumulative VPN connections, 80,916,129.90 GB)
    No. 2: Iran (ISLAMIC Republic Of) (3,969,537,843 cumulative VPN connections, 59,063,960.97 GB)
    No. 3: United States (677,618,424 cumulative VPN connections, 33,966,429.27 GB)
    No. 4: Japan (430,594,921 cumulative VPN connections, 33,439,267.88 GB)
    No. 5: Taiwan (648,625,527 cumulative VPN connections, 30,523,072.62 GB)
    No. 6: France (368,793,618 cumulative VPN connections, 29,722,488.10 GB)
    No. 7: India (589,445,238 cumulative VPN connections, 25,820,831.96 GB)
    No. 8: Indonesia (713,794,702 cumulative VPN connections, 22,978,944.77 GB)
    No. 9: Russian Federation (472,826,892 cumulative VPN connections, 22,701,898.81 GB)
    No. 10: China (725,097,820 cumulative VPN connections, 21,653,050.05 GB)
""".trimIndent()
