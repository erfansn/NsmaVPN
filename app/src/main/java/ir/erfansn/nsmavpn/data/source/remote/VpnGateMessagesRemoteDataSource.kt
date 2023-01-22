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

    override suspend fun fetchLatestMessageBodyText(userAccountId: String): String {
        val threadId = fetchLatestThreadIdFromVpnGate(userAccountId) ?: throw NoVpnGateSubscribed()

        val data: Message = withContext(ioDispatcher) {
            api.selectAccount(userAccountId)
                .users()
                .messages()
                .get("me", threadId)
                .execute()
        }

        return data.payload.body.decodeData().decodeToString()
    }

    override suspend fun userIsSubscribedToVpnGateDailyMail(userAccountId: String): Boolean {
        return fetchLatestThreadIdFromVpnGate(userAccountId) != null
    }

    private suspend fun fetchLatestThreadIdFromVpnGate(userAccountId: String) =
        withContext(ioDispatcher) {
            val data = api
                .selectAccount(userAccountId)
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
    suspend fun fetchLatestMessageBodyText(userAccountId: String): String
    suspend fun userIsSubscribedToVpnGateDailyMail(userAccountId: String): Boolean
}

class NoVpnGateSubscribed :
    Exception("Your email don't have subscribe to VpnGate daily mirror links")
