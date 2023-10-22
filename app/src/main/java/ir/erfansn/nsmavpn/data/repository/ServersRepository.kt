package ir.erfansn.nsmavpn.data.repository

import android.content.res.Resources.NotFoundException
import ir.erfansn.nsmavpn.data.source.local.LastVpnConnectionLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.VpnProviderLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.LastVpnConnection
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.UrlParts
import ir.erfansn.nsmavpn.data.source.remote.VpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.util.PingChecker
import ir.erfansn.nsmavpn.data.util.PingChecker.Companion.NOT_AVAILABLE
import ir.erfansn.nsmavpn.data.util.VpnGateContentExtractor
import ir.erfansn.nsmavpn.data.util.asyncFilter
import ir.erfansn.nsmavpn.data.util.asyncMinByOrNull
import ir.erfansn.nsmavpn.data.util.asyncPartition
import ir.erfansn.nsmavpn.data.util.runWithContext
import ir.erfansn.nsmavpn.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ServersRepository @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val vpnGateMessagesRemoteDataSource: VpnGateMessagesRemoteDataSource,
    private val vpnProviderLocalDataSource: VpnProviderLocalDataSource,
    private val lastVpnConnectionLocalDataSource: LastVpnConnectionLocalDataSource,
    private val vpnGateContentExtractor: VpnGateContentExtractor,
    private val pingChecker: PingChecker,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun obtainFastestVpnServer(): Server {
        val lastVpnConnection = lastVpnConnectionLocalDataSource.lastVpnConnection.first()
        if (lastVpnConnection.isConnectionValidYet()) return lastVpnConnection.server

        return vpnProviderLocalDataSource
            .getAvailableVpnServers()
            .asyncMinByOrNull { server ->
                pingChecker.measure(server.address.hostName).takeIf { it != NOT_AVAILABLE } ?: Double.MAX_VALUE
            } ?: throw NoAvailableVpnServerException()
    }

    private fun LastVpnConnection.isConnectionValidYet(): Boolean {
        return System.currentTimeMillis() - epochTime <= SERVER_VALIDATION_TIMEOUT_MS
    }

    suspend fun collectVpnServers() {
        val userEmailAddress = profileRepository.userProfile.first().emailAddress

        fun UrlParts.toAbsoluteUrl(): String {
            return "${protocol.name.lowercase()}://$hostName:$portNumber"
        }
        val vpnProviderAddress = obtainVpnProviderUrl(userEmailAddress).toAbsoluteUrl()

        val (availableServers, unavailableServers) = vpnGateContentExtractor
            .extractSstpVpnServers(vpnProviderAddress)
            .runWithContext(ioDispatcher) {
                asyncPartition { pingChecker.isReachable(it.address.hostName) }
            }

        vpnProviderLocalDataSource.saveVpnServers(availableServers)
        vpnProviderLocalDataSource.blockVpnServers(*unavailableServers.toTypedArray())
    }

    private suspend fun obtainVpnProviderUrl(userEmailAddress: String): UrlParts {
        val vpnProvider = vpnProviderLocalDataSource.getVpnProviderAddress()
        if (pingChecker.isReachable(vpnProvider.hostName)) return vpnProvider

        val messageIds = vpnGateMessagesRemoteDataSource.fetchMessageIdList(userEmailAddress) ?: throw NoVpnGateSubscribed()

        val mirrorLink = messageIds.map {
            vpnGateMessagesRemoteDataSource.fetchMessageBodyText(
                emailAddress = userEmailAddress,
                messageId = it,
            )
        }.flatMap { messageBodyText ->
            vpnGateContentExtractor.findVpnGateMirrorLinks(messageBodyText)
        }.runWithContext(ioDispatcher) {
            asyncMinByOrNull { pingChecker.measure(it.hostName) }
        } ?: throw NotFoundException()

        vpnProviderLocalDataSource.saveVpnProviderAddress(mirrorLink)
        return vpnProviderLocalDataSource.getVpnProviderAddress()
    }

    suspend fun blockVpnServer(server: Server) {
        vpnProviderLocalDataSource.blockVpnServers(server)
    }

    suspend fun blockUnreachableVpnServers() {
        val unreachableVpnServers = vpnProviderLocalDataSource
            .getAvailableVpnServers()
            .asyncFilter { !pingChecker.isReachable(it.address.hostName) }
            .toTypedArray()

        vpnProviderLocalDataSource.blockVpnServers(*unreachableVpnServers)
    }

    suspend fun unblockFirstAvailableVpnServerFromBlacklist() {
        val reachableServers = vpnProviderLocalDataSource
            .getBlockedVpnServers()
            .asyncFilter { pingChecker.isReachable(it.address.hostName) }

        vpnProviderLocalDataSource.unblockVpnServers(reachableServers)
    }

    companion object {
        private const val SERVER_VALIDATION_TIMEOUT_MS = 30 * 60 * 1000L
    }
}

private fun UrlLink.toAbsoluteLink(): String {
    return "${protocol.name.lowercase()}://$hostName:$portNumber"
}

class NoAvailableVpnServerException : Exception("Available servers list is empty")
class NoVpnGateSubscribed :
    Exception("Your email don't have subscribe to VpnGate daily mirror links")
