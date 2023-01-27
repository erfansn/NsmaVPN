package ir.erfansn.nsmavpn.data.source.remote

import com.google.api.services.gmail.model.Message
import ir.erfansn.nsmavpn.data.source.remote.api.GmailApi
import ir.erfansn.nsmavpn.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultVpnGateMessagesRemoteDataSource @Inject constructor(
    private val api: GmailApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : VpnGateMessagesRemoteDataSource {

    override suspend fun fetchLatestMessageBodyText(emailAddress: String): String {
        val threadId = fetchLatestThreadIdFromVpnGate(emailAddress) ?: throw NoVpnGateSubscribed()

        val data: Message = withContext(ioDispatcher) {
            api.selectAccount(emailAddress)
                .users()
                .messages()
                .get("me", threadId)
                .execute()
        }

        return data.payload.body.decodeData().decodeToString()
    }

    override suspend fun isSubscribedToDailyMail(emailAddress: String): Boolean {
        return fetchLatestThreadIdFromVpnGate(emailAddress) != null
    }

    private suspend fun fetchLatestThreadIdFromVpnGate(emailAddress: String) =
        withContext(ioDispatcher) {
            val data = api.selectAccount(emailAddress)
                .users()
                .messages()
                .list("me")
                .setMaxResults(1)
                .setQ("from:$VPNGATE_EMAIL")
                .execute()

            data.messages.firstOrNull()?.threadId
        }

    companion object {
        private const val VPNGATE_EMAIL = "vpngate-daily@vpngate.net"
    }
}

interface VpnGateMessagesRemoteDataSource {
    suspend fun fetchLatestMessageBodyText(emailAddress: String): String
    suspend fun isSubscribedToDailyMail(emailAddress: String): Boolean
}

class NoVpnGateSubscribed :
    Exception("Your email don't have subscribe to VpnGate daily mirror links")
