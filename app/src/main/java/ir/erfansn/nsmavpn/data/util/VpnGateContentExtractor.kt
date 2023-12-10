package ir.erfansn.nsmavpn.data.util

import ir.erfansn.nsmavpn.data.model.MirrorLink
import ir.erfansn.nsmavpn.data.source.local.datastore.Protocol
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.server
import ir.erfansn.nsmavpn.data.source.local.datastore.urlParts
import ir.erfansn.nsmavpn.di.IoDispatcher
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.Doc
import it.skrape.selects.eachText
import it.skrape.selects.html5.img
import it.skrape.selects.html5.td
import it.skrape.selects.html5.tr
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultVpnGateContentExtractor @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : VpnGateContentExtractor {

    override suspend fun extractSstpVpnServers(vpnGateUrl: String) = withContext(ioDispatcher) {
        skrape(AsyncFetcher) {
            request {
                url = vpnGateUrl
                timeout = 10_000
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
                                    protocol = Protocol.HTTPS
                                    hostName = urlParts.first()
                                    portNumber = urlParts.getOrNull(1)?.toInt() ?: 443
                                }
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

    override fun findVpnGateMirrorLinks(content: String) = VPN_GATE_MIRROR_LINK_REGEX
        .findAll(content)
        .map(MatchResult::destructured)
        .map { (protocol, host, port) -> MirrorLink(protocol, host, port) }
        .toList()

    companion object {
        private val VPN_GATE_MIRROR_LINK_REGEX = """(\w+)://(.*):(\d{1,5})""".toRegex()
    }
}

interface VpnGateContentExtractor {
    suspend fun extractSstpVpnServers(vpnGateUrl: String): List<Server>
    fun findVpnGateMirrorLinks(content: String): List<MirrorLink>
}
