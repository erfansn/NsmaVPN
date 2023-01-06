package ir.erfansn.nsmavpn.data.repository

import android.content.res.Resources.NotFoundException
import ir.erfansn.nsmavpn.data.source.local.VpnProviderLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.UrlLink
import ir.erfansn.nsmavpn.data.source.remote.VpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.task.ServersTasksDataSource
import ir.erfansn.nsmavpn.data.util.*
import javax.inject.Inject

class ServersRepository @Inject constructor(
    private val vpnGateMessagesRemoteDataSource: VpnGateMessagesRemoteDataSource,
    private val vpnProviderLocalDataSource: VpnProviderLocalDataSource,
    private val vpnGateContentExtractor: VpnGateContentExtractor,
    private val pingChecker: PingChecker,
    private val serversTasksDataSource: ServersTasksDataSource,
) {
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
            .extractSstpVpnServers(vpnProviderAddress.toAbsoluteLink())
            .asyncPartition {
                pingChecker.measure(it.address.hostName).isNaN()
            }

        vpnProviderLocalDataSource.blockVpnServers(blackServers)
        vpnProviderLocalDataSource.saveVpnServers(availableServers)
    }

    private suspend fun obtainVpnProviderAddress(userAccountId: String): UrlLink {
        val vpnProvider = vpnProviderLocalDataSource.getVpnProviderAddress()
        if (pingChecker.isReachable(vpnProvider.hostName)) return vpnProvider

        // TODO: if mirror links are unavailable check second email message
        val bodyText = vpnGateMessagesRemoteDataSource.fetchLatestMessageBodyText(userAccountId)
        val mirrorLink = vpnGateContentExtractor.findVpnGateMirrorLinks(bodyText).asyncMinByOrNull {
            pingChecker.isReachable(it.hostName)
        } ?: throw NotFoundException()

        vpnProviderLocalDataSource.updateVpnProviderAddress(mirrorLink.asUrlLink())
        return vpnProviderLocalDataSource.getVpnProviderAddress()
    }

    suspend fun unblockAvailableVpnServerFromBlacklistRandomly() {
        val server = vpnProviderLocalDataSource.getBlockedVpnServers().randomOrNull() ?: return

        if (!pingChecker.measure(server.address.hostName).isNaN()) {
            vpnProviderLocalDataSource.unblockVpnServer(server)
        }
    }

    suspend fun userIsSubscribedToVpnGateDailyMail(userId: String) =
        vpnGateMessagesRemoteDataSource.userIsSubscribedToVpnGateDailyMail(userId)

    fun beginVpnServersWorker() {
        serversTasksDataSource.collectVpnServerPeriodically()
        serversTasksDataSource.removeAvailableVpnServerFromBlacklistPeriodically()
    }

    companion object {
        private const val SELECTED_SERVER_TIMEOUT_MS = 30 * 60 * 1000
    }
}

private fun UrlLink.toAbsoluteLink(): String {
    return "${protocol.name.lowercase()}://$hostName:$portNumber"
}

class VpnServersNotExistsException : Exception("Vpn servers list is empty")
