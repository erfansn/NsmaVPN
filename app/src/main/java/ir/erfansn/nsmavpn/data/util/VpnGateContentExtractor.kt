package ir.erfansn.nsmavpn.data.util

import ir.erfansn.nsmavpn.data.source.local.datastore.Protocol
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.UrlLink
import ir.erfansn.nsmavpn.di.IoDispatcher
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachText
import it.skrape.selects.html5.td
import it.skrape.selects.html5.tr
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultVpnGateContentExtractor @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : VpnGateContentExtractor {

    override suspend fun extractSstpVpnServers(address: String) = withContext(ioDispatcher) {
        skrape(AsyncFetcher) {
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
                            Server.newBuilder()
                                .setCountry(country)
                                .setAddress(url.toUrlLink())
                                .build()
                        }
                }
            }
        }
    }

    private fun String.toUrlLink(): UrlLink {
        val urlParts = split(":")
        return UrlLink.newBuilder()
            .setProtocol(Protocol.HTTPS)
            .setHostName(urlParts.first())
            .setPortNumber(urlParts.getOrNull(1)?.toInt() ?: 443)
            .build()
    }

    override fun findVpnGateMirrorLinks(content: String) = VPN_GATE_MIRROR_LINK_REGEX
        .findAll(content)
        .map {
            val (protocol, host, port) = it.destructured
            MirrorLink(protocol, host, port)
        }
        .toList()

    companion object {
        val VPN_GATE_MIRROR_LINK_REGEX = """(\w+)://(.*):(\d{1,5})""".toRegex()
    }
}

interface VpnGateContentExtractor {
    suspend fun extractSstpVpnServers(address: String): List<Server>
    fun findVpnGateMirrorLinks(content: String): List<MirrorLink>
}

data class MirrorLink(
    val protocol: String,
    val hostName: String,
    val port: String,
) {
    override fun toString(): String {
        return "$protocol://$hostName:$port"
    }
}

fun MirrorLink.asUrlLink(): UrlLink = UrlLink.newBuilder()
    .setProtocolValue(if (protocol == "http") 0 else 1)
    .setHostName(hostName)
    .setPortNumber(port.toInt())
    .build()
