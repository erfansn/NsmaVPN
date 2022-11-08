package ir.erfansn.nsmavpn.data.source.remote

import ir.erfansn.nsmavpn.data.source.remote.api.GmailApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultVpnGateMessagesRemoteDataSource @Inject constructor(
    private val api: GmailApi,
    private val ioDispatcher: CoroutineDispatcher
) : VpnGateMessagesRemoteDataSource {

    override suspend fun fetchLatestMessageBodyText(userId: String) = withContext(ioDispatcher) {
        val data = api
            .selectAccount(userId)
            .users()
            .messages()
            .list("me")
            .setMaxResults(1)
            .setQ("from:$VPNGATE_EMAIL")
            .execute()

        val firstMessage = data.messages.singleOrNull()
            ?: throw NoSuchElementException("Your email don't have subscribe to VpnGate daily mirror links")

        firstMessage.payload.body.decodeData().decodeToString()
    }

    companion object {
        private const val VPNGATE_EMAIL = "vpngate-daily@vpngate.net"
    }
}

interface VpnGateMessagesRemoteDataSource {
    suspend fun fetchLatestMessageBodyText(userId: String): String
}
