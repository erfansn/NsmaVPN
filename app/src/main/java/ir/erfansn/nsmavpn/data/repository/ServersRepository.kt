package ir.erfansn.nsmavpn.data.repository

import android.content.res.Resources.NotFoundException
import ir.erfansn.nsmavpn.data.source.local.VpnProviderLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.source.local.datastore.UrlLink
import ir.erfansn.nsmavpn.data.source.remote.VpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.task.ServersTasksDataSource
import ir.erfansn.nsmavpn.data.util.PingChecker
import ir.erfansn.nsmavpn.data.util.VpnGateContentExtractor
import ir.erfansn.nsmavpn.data.util.asUrlLink
import ir.erfansn.nsmavpn.data.util.asyncMinByOrNull
import ir.erfansn.nsmavpn.data.util.asyncPartition
import ir.erfansn.nsmavpn.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ServersRepository @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val vpnGateMessagesRemoteDataSource: VpnGateMessagesRemoteDataSource,
    private val vpnProviderLocalDataSource: VpnProviderLocalDataSource,
    private val serversTasksDataSource: ServersTasksDataSource,
    private val vpnGateContentExtractor: VpnGateContentExtractor,
    private val pingChecker: PingChecker,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private lateinit var currentVpnServer: Server
    private var vpnServerSelectionTimeMs: Long = 0L

    suspend fun getFastestVpnServer(): Server {
        if (selectedVpnServerIsValid) return currentVpnServer

        val vpnServer = vpnProviderLocalDataSource.getAvailableVpnServers().asyncMinByOrNull(ioDispatcher) { server ->
            pingChecker.measure(server.address.hostName)
        } ?: throw VpnServersNotExistsException()

        currentVpnServer = vpnServer
        vpnServerSelectionTimeMs = System.currentTimeMillis()
        return vpnServer
    }

    private val selectedVpnServerIsValid get() =
        System.currentTimeMillis() - vpnServerSelectionTimeMs <= SERVER_VALIDATION_TIMEOUT_MS

    suspend fun collectVpnServers() {
        val userAccountEmail = profileRepository.userProfile.first().emailAddress
        val vpnProviderAddress = obtainVpnProviderAddress(userAccountEmail)

        val (blackServers, availableServers) = vpnGateContentExtractor
            .extractSstpVpnServers(vpnProviderAddress.toAbsoluteLink())
            .asyncPartition(ioDispatcher) {
                pingChecker.measure(it.address.hostName).isNaN()
            }

        vpnProviderLocalDataSource.blockVpnServers(*blackServers.toTypedArray())
        vpnProviderLocalDataSource.saveVpnServers(availableServers)
    }

    private suspend fun obtainVpnProviderAddress(userAccountEmail: String): UrlLink {
        val vpnProvider = vpnProviderLocalDataSource.getVpnProviderAddress()
        if (pingChecker.isReachable(vpnProvider.hostName)) return vpnProvider

        // TODO: if mirror links are unavailable check second email message
        val messageIds = vpnGateMessagesRemoteDataSource.fetchMessageIdList(userAccountEmail) ?: throw NoVpnGateSubscribed()
        val bodyText = messageIds.map {
            vpnGateMessagesRemoteDataSource.fetchMessageBodyText(
                emailAddress = userAccountEmail,
                messageId = it,
            )
        }.joinToString(separator = "\n")
        val mirrorLink = vpnGateContentExtractor.findVpnGateMirrorLinks(bodyText).asyncMinByOrNull(ioDispatcher) {
            pingChecker.isReachable(it.hostName)
        } ?: throw NotFoundException()

        vpnProviderLocalDataSource.saveVpnProviderAddress(mirrorLink.asUrlLink())
        return vpnProviderLocalDataSource.getVpnProviderAddress()
    }

    suspend fun unblockAvailableVpnServerFromBlacklistRandomly() {
        val server = vpnProviderLocalDataSource.getBlockedVpnServers().randomOrNull() ?: return

        if (!pingChecker.measure(server.address.hostName).isNaN()) {
            vpnProviderLocalDataSource.unblockVpnServer(server)
        }
    }

    suspend fun isSubscribedToVpnGateDailyMail(emailAddress: String) =
        false

    fun beginVpnServersWorker() {
        serversTasksDataSource.collectVpnServerPeriodically()
        serversTasksDataSource.removeAvailableVpnServerFromBlacklistPeriodically()
    }

    fun stopVpnServersWorker() {
        // serversTasksDataSource.cancelAllWorker()
    }

    fun isCollectingVpnServers(): Flow<Boolean> {
        // Return as flow
        // serversTasksDataSource.isCollectingServers()
        return flowOf(true)
    }

    suspend fun blockVpnServer(server: Server) {
        vpnProviderLocalDataSource.blockVpnServers(server)
    }

    companion object {
        private const val SERVER_VALIDATION_TIMEOUT_MS = 30 * 60 * 1000L
    }
}

private fun UrlLink.toAbsoluteLink(): String {
    return "${protocol.name.lowercase()}://$hostName:$portNumber"
}

class VpnServersNotExistsException : Exception("Vpn servers list is empty")
class NoVpnGateSubscribed :
    Exception("Your email don't have subscribe to VpnGate daily mirror links")
