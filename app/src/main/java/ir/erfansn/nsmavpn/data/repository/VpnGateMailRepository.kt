package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.source.remote.VpnGateMailMessagesRemoteDataSource
import java.net.URL
import javax.inject.Inject

interface VpnGateMailRepository {
    suspend fun isSubscribedToDailyMail(emailAddress: String): Boolean
    suspend fun obtainMirrorSiteAddresses(emailAddress: String, lastDay: Int = 0): List<URL>
}

class DefaultVpnGateMailRepository @Inject constructor(
    private val vpnGateMailMessagesRemoteDataSource: VpnGateMailMessagesRemoteDataSource
) : VpnGateMailRepository {

    override suspend fun isSubscribedToDailyMail(emailAddress: String): Boolean {
        val recentMessageId = vpnGateMailMessagesRemoteDataSource.fetchMessageIdList(
            emailAddress = emailAddress,
            lastDay = 0
        ).firstOrNull() ?: return false

        return vpnGateMailMessagesRemoteDataSource.fetchMessage(
            emailAddress = emailAddress,
            messageId = recentMessageId,
        ).let {
            fun Long.toHours() = this / (60 * 60 * 1000)
            // Probably should use NTP to get the correct time
            val lastReceivedMessageTime = System.currentTimeMillis() - it.internalDate
            lastReceivedMessageTime.toHours() <= 12
        }
    }

    override suspend fun obtainMirrorSiteAddresses(emailAddress: String, lastDay: Int): List<URL> {
        return vpnGateMailMessagesRemoteDataSource.fetchMessageIdList(
            emailAddress = emailAddress,
            lastDay = lastDay,
        ).also {
            if (it.isEmpty()) throw NoVpnGateSubscriptionException(emailAddress)
        }.map {
            vpnGateMailMessagesRemoteDataSource.fetchMessageBodyText(
                emailAddress = emailAddress,
                messageId = it,
            )
        }.flatMap { messageBodyText ->
            findVpnGateMirrorLinks(messageBodyText)
        }
    }

    private fun findVpnGateMirrorLinks(content: String) = """http://.*:\d{1,5}""".toRegex()
        .findAll(content)
        .map(MatchResult::value)
        .map { URL(it) }
        .toList()
}

class NoVpnGateSubscriptionException(emailAddress: String) : Exception("$emailAddress isn't subscribed to VpnGate daily mirror links")
