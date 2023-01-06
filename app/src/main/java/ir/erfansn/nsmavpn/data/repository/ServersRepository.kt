package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.source.local.VpnProviderLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.remote.VpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.task.ServersTasksDataSource
import ir.erfansn.nsmavpn.data.util.LinkAvailabilityChecker
import ir.erfansn.nsmavpn.data.util.PingChecker
import ir.erfansn.nsmavpn.data.util.VpnGateContentExtractor
import ir.erfansn.nsmavpn.data.util.asyncMinByOrNull
import javax.inject.Inject

class ServersRepository @Inject constructor(
    private val vpnGateMessagesRemoteDataSource: VpnGateMessagesRemoteDataSource,
    private val vpnProviderLocalDataSource: VpnProviderLocalDataSource,
    private val linkAvailabilityChecker: LinkAvailabilityChecker,
    private val vpnGateContentExtractor: VpnGateContentExtractor,
    private val pingChecker: PingChecker,
    serversTasksDataSource: ServersTasksDataSource,
) {
    init {
        serversTasksDataSource.collectVpnServerPeriodically()
        serversTasksDataSource.removeAvailableVpnServerFromBlacklistPeriodically()
    }

    private lateinit var currentVpnServer: Server
    private var vpnServerSelectionTimeMs: Long = 0L

    suspend fun getFastestVpnServer(): Server {
        if (selectedVpnServerIsValid) return currentVpnServer

        val vpnServer = vpnProviderLocalDataSource.getAvailableVpnServers().asyncMinByOrNull { server ->
            pingChecker.measure(server.address.hostName)
        } ?: throw VpnServersNotExistsException()

        currentVpnServer = vpnServer
        vpnServerSelectionTimeMs = System.currentTimeMillis()
        return vpnServer
    }

    private val selectedVpnServerIsValid get() =
        System.currentTimeMillis() - vpnServerSelectionTimeMs <= SELECTED_SERVER_TIMEOUT_MS

    suspend fun collectVpnServers(userAccountId: String) {
        val vpnProviderAddress = obtainVpnProviderAddress(userAccountId)

        val (blackServers, availableServers) = vpnGateContentExtractor
            .extractSstpVpnServers(vpnProviderAddress)
            .partition {
                pingChecker.measure(it.address.hostName) != Int.MAX_VALUE
            }

        vpnProviderLocalDataSource.blockVpnServers(blackServers)
        vpnProviderLocalDataSource.saveVpnServers(availableServers)
    }

    private suspend fun obtainVpnProviderAddress(userAccountId: String): UrlLink {
        val vpnProvider = vpnProviderLocalDataSource.getVpnProviderAddress()
        if (vpnProvider.hostName.isNotEmpty() && linkAvailabilityChecker.checkLink(vpnProvider.toAbsoluteLink())) {
            return vpnProvider
        }

        val bodyText = vpnGateMessagesRemoteDataSource.fetchLatestMessageBodyText(userId)
        val mirrorLink = vpnGateContentExtractor.findVpnGateMirrorLinks(bodyText).first {
            linkAvailabilityChecker.checkLink(it)
        }

        vpnProviderLocalDataSource.updateVpnProviderAddress(mirrorLink.asUrlLink())
        return vpnProviderLocalDataSource.getVpnProviderAddress()
    }

    suspend fun unblockAvailableVpnServerFromBlacklistRandomly() {
        val server = vpnProviderLocalDataSource.getBlockedVpnServers().randomOrNull() ?: return

        if (pingChecker.measure(server.hostName) != Int.MAX_VALUE) {
            vpnProviderLocalDataSource.unblockVpnServer(server)
        }
    }

    companion object {
        private const val SELECTED_SERVER_TIMEOUT_MS = 30 * 60 * 1000
    }
}

private fun UrlLink.toAbsoluteLink(): String {
    return "${protocol.name.lowercase()}://$hostName:$portNumber"
}

class VpnServersNotExistsException : Exception("Vpn servers list is empty")
