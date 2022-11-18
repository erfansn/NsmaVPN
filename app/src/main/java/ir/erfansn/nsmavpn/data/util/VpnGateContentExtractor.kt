package ir.erfansn.nsmavpn.data.util

import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachText
import it.skrape.selects.html5.td
import it.skrape.selects.html5.tr
import javax.inject.Inject

class DefaultVpnGateContentExtractor @Inject constructor() : VpnGateContentExtractor {

    override fun extractSstpVpnServers(address: String): List<Server> {
        return skrape(HttpFetcher) {
            request { url = address }
            response {
                htmlDocument {
                    val countrySelector = tr {
                        td(":first-child") {
                            withAttribute = "style" to "text-align: center;"

                            findAll { eachText }
                        }
                    }
                    val serverSelector = tr {
                        td(":nth-child(8)") {
                            withAttribute =
                                "style" to "text-align: center; word-break: break-all; white-space: normal;"

                            findAll {
                                eachText.map { it.substringAfter(":").trim() }
                            }
                        }
                    }

                    countrySelector.zip(serverSelector).filterNot { it.second.isEmpty() }
                        .map { (country, url) ->
                            val urlParts = url.split(":")
                            Server.newBuilder()
                                .setCountry(country)
                                .setHostName(urlParts.first())
                                .setPort(urlParts.getOrNull(1) ?: "443")
                                .build()
                        }
                }
            }
        }
    }
}

interface VpnGateContentExtractor {
    fun extractSstpVpnServers(address: String): List<Server>
}
