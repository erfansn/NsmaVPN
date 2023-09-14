package ir.erfansn.nsmavpn.data.source.remote

import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import ir.erfansn.nsmavpn.data.source.remote.api.GoogleApi
import ir.erfansn.nsmavpn.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultVpnGateMessagesRemoteDataSource @Inject constructor(
    private val api: GoogleApi<Gmail>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : VpnGateMessagesRemoteDataSource {

    override suspend fun fetchMessageIdList(emailAddress: String) =
        withContext(ioDispatcher) {
            val data = api.selectAccount(emailAddress)
                .users()
                .messages()
                .list("me")
                .setMaxResults(MESSAGE_COUNT)
                .setQ("from:$VPN_GATE_EMAIL")
                .execute()

            data?.messages?.map { it.id }
        }

    override suspend fun fetchMessageBodyText(emailAddress: String, messageId: String): String {
        val message = fetchMessage(emailAddress, messageId)
        return message.payload.body.decodeData().decodeToString()
    }

    override suspend fun fetchMessage(emailAddress: String, messageId: String): Message =
        withContext(ioDispatcher) {
            api.selectAccount(emailAddress)
                .users()
                .messages()
                .get("me", messageId)
                .execute()
        }

    companion object {
        private const val VPN_GATE_EMAIL = "vpngate-daily@vpngate.net"
        private const val MESSAGE_COUNT = 3L
    }
}

interface VpnGateMessagesRemoteDataSource {
    suspend fun fetchMessageIdList(emailAddress: String): List<String>?
    suspend fun fetchMessageBodyText(emailAddress: String, messageId: String): String
    suspend fun fetchMessage(emailAddress: String, messageId: String): Message
}
