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

import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.server
import ir.erfansn.nsmavpn.data.source.local.datastore.urlParts
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.Doc
import it.skrape.selects.eachText
import it.skrape.selects.html5.img
import it.skrape.selects.html5.td
import it.skrape.selects.html5.tr
import java.net.URL
import javax.inject.Inject

interface VpnGateContentExtractor {
    suspend fun extractSstpVpnServers(vpnGateAddress: URL = URL("https://vpngate.net")): List<Server>
}

class DefaultVpnGateContentExtractor @Inject constructor() : VpnGateContentExtractor {

    override suspend fun extractSstpVpnServers(vpnGateAddress: URL) =
        skrape(AsyncFetcher) {
            request {
                url = vpnGateAddress.toExternalForm()
                timeout = 5_000
            }
            response {
                htmlDocument {
                    countryFlagImageUrlSelector.zip(serverUrlSelector)
                        .filter { (_, serverUrl) -> serverUrl.isNotEmpty() }
                        .map { (countryFlagUrl, serverUrl) ->
                            val urlParts = serverUrl.split(":")

                            server {
                                countryCode = countryFlagUrl.substringAfterLast("/").split(".")[0]
                                address = urlParts {
                                    hostName = urlParts.first()
                                    portNumber = urlParts.getOrNull(1)?.toInt() ?: 443
                                }
                            }
                        }
                }
            }
        }

    private val Doc.countryFlagImageUrlSelector
        get() = tr {
            td(":first-child") {
                withAttribute = "style" to "text-align: center;"

                img {
                    findAll {
                        map { it attribute "src" }
                    }
                }
            }
        }

    private val Doc.serverUrlSelector
        get() = tr {
            td(":nth-child(8)") {
                withAttribute =
                    "style" to "text-align: center; word-break: break-all; white-space: normal;"

                "p span b span" {
                    "style" to "color: #006600;"

                    findAll { eachText }
                }
            }
        }
}
