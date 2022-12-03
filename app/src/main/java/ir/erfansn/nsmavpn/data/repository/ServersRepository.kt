package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.source.local.VpnProviderLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.remote.VpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.util.LinkAvailabilityChecker
import ir.erfansn.nsmavpn.data.util.PingChecker
import ir.erfansn.nsmavpn.data.util.VpnGateContentExtractor
import ir.erfansn.nsmavpn.util.asyncMinByOrNull
import javax.inject.Inject

class ServersRepository @Inject constructor(
    private val vpnGateMessagesRemoteDataSource: VpnGateMessagesRemoteDataSource,
    private val vpnProviderLocalDataSource: VpnProviderLocalDataSource,
    private val linkAvailabilityChecker: LinkAvailabilityChecker,
    private val vpnGateContentExtractor: VpnGateContentExtractor,
    private val pingChecker: PingChecker,
) {
    private lateinit var currentVpnServer: Server
    private var vpnServerSelectionTimeMs: Long = 0L

    suspend fun getFastestVpnServer(): Server {
        if (selectedVpnServerIsValid) return currentVpnServer

        return vpnProviderLocalDataSource.getVpnServers().asyncMinByOrNull { server ->
            pingChecker.measure(server.hostName)
        }?.also {
            currentVpnServer = it
            vpnServerSelectionTimeMs = System.currentTimeMillis()
        } ?: throw VpnServersNotExistsException()
    }

    private val selectedVpnServerIsValid get() =
        System.currentTimeMillis() - vpnServerSelectionTimeMs <= SELECTED_SERVER_TIMEOUT_MS

    suspend fun collectVpnServers(userId: String) {
        val vpnProviderAddress = obtainVpnProviderAddress(userId)
        val (blackServers, availableServers) = vpnGateContentExtractor
            .extractSstpVpnServers(vpnProviderAddress)
            .partition {
                pingChecker.measure(it.hostName) != Int.MAX_VALUE
            }

        vpnProviderLocalDataSource.blockVpnServers(blackServers)
        vpnProviderLocalDataSource.saveVpnServers(availableServers)
    }

    private suspend fun obtainVpnProviderAddress(userId: String): String {
        val vpnProvider = vpnProviderLocalDataSource.getVpnProviderAddress()
        if (vpnProvider.isNotEmpty() && linkAvailabilityChecker.checkLink(vpnProvider)) {
            return vpnProvider
        }

        val bodyText = vpnGateMessagesRemoteDataSource.fetchLatestMessageBodyText(userId)
        val mirrorLink = vpnGateContentExtractor.findVpnGateMirrorLinks(bodyText).first {
            linkAvailabilityChecker.checkLink(it)
        }

        vpnProviderLocalDataSource.updateVpnProviderAddress(mirrorLink)
        return vpnProviderLocalDataSource.getVpnProviderAddress()
    }

    companion object {
        private const val SELECTED_SERVER_TIMEOUT_MS = 30 * 60 * 1000
    }
}

class VpnServersNotExistsException : Throwable()
