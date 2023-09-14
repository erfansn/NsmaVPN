package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.source.remote.VpnGateMessagesRemoteDataSource
import javax.inject.Inject

class DefaultVpnGateMailRepository @Inject constructor(
    private val vpnGateMessagesRemoteDataSource: VpnGateMessagesRemoteDataSource
) : VpnGateMailRepository {

    override suspend fun isSubscribedToDailyMail(emailAddress: String): Boolean {
        val recentMessageId = vpnGateMessagesRemoteDataSource.fetchMessageIdList(emailAddress)?.firstOrNull() ?: return false

        return vpnGateMessagesRemoteDataSource.fetchMessage(
            emailAddress = emailAddress,
            messageId = recentMessageId,
        ).let {
            fun Long.toHours() = this / (60 * 60 * 1000)

            val lastReceivedMessageInMs = System.currentTimeMillis() - it.internalDate
            lastReceivedMessageInMs.toHours() <= 12
        }
    }
}

interface VpnGateMailRepository {
    suspend fun isSubscribedToDailyMail(emailAddress: String): Boolean
}
