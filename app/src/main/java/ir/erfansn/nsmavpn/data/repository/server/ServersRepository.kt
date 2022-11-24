package ir.erfansn.nsmavpn.data.repository.server

import ir.erfansn.nsmavpn.data.source.local.VpnProviderLocalDataSource
import ir.erfansn.nsmavpn.data.source.remote.VpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.util.LinkAvailabilityChecker
import ir.erfansn.nsmavpn.data.util.VpnGateContentExtractor
import javax.inject.Inject

class ServersRepository @Inject constructor(
    private val vpnGateMessagesRemoteDataSource: VpnGateMessagesRemoteDataSource,
    private val vpnProviderLocalDataSource: VpnProviderLocalDataSource,
    private val linkAvailabilityChecker: LinkAvailabilityChecker,
    private val vpnGateContentExtractor: VpnGateContentExtractor,
) {
    suspend fun collectVpnServers(userId: String) {
        val vpnProviderAddress = obtainVpnProviderAddress(userId)
        val servers = vpnGateContentExtractor.extractSstpVpnServers(vpnProviderAddress)
        vpnProviderLocalDataSource.saveVpnServers(servers)
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
}