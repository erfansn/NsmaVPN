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

    override fun findVpnGateMirrorLinks(content: String) = VPN_GATE_MIRROR_LINK_REGEX
        .findAll(content)
        .map { it.value }
        .toList()

    companion object {
        private val VPN_GATE_MIRROR_LINK_REGEX = """(http://(?:\d+.){3}\d+:\d{1,5})""".toRegex()
    }
}

interface VpnGateContentExtractor {
    fun extractSstpVpnServers(address: String): List<Server>
    fun findVpnGateMirrorLinks(content: String): List<String>
}
