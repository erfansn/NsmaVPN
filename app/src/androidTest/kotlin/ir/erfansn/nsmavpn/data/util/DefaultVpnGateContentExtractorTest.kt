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

