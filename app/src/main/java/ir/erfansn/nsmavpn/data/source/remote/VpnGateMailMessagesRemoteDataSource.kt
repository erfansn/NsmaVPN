package ir.erfansn.nsmavpn.data.source.remote

import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import ir.erfansn.nsmavpn.data.source.remote.api.GoogleApi
import ir.erfansn.nsmavpn.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface VpnGateMailMessagesRemoteDataSource {
    suspend fun fetchMessageIdList(emailAddress: String, lastDay: Int = 0): List<String>
    suspend fun fetchMessageBodyText(emailAddress: String, messageId: String): String
    suspend fun fetchMessage(emailAddress: String, messageId: String): Message
}

class DefaultVpnGateMailMessagesRemoteDataSource @Inject constructor(
    private val api: GoogleApi<Gmail>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : VpnGateMailMessagesRemoteDataSource {

    override suspend fun fetchMessageIdList(emailAddress: String, lastDay: Int) =
        withContext(ioDispatcher) {
            val data = api.selectAccount(emailAddress)
                .users()
                .messages()
                .list("me")
                .setQ("from:$VPN_GATE_EMAIL older_than:${lastDay}d")
                .setMaxResults(DAILY_MESSAGE_COUNT)
                .execute()

            data?.messages?.map { it.id.toString() }.orEmpty()
        }

    override suspend fun fetchMessageBodyText(emailAddress: String, messageId: String): String {
        val message = fetchMessage(emailAddress, messageId)
        return message.payload.body.decodeData().decodeToString()
    }

    override suspend fun fetchMessage(emailAddress: String, messageId: String) =
        withContext(ioDispatcher) {
            val message = api.selectAccount(emailAddress)
                .users()
                .messages()
                .get("me", messageId)
                .execute()

            message ?: throw NoValidMessageId(messageId)
        }

    companion object {
        private const val VPN_GATE_EMAIL = "vpngate-daily@vpngate.net"
        private const val DAILY_MESSAGE_COUNT = 3L
    }
}
class NoValidMessageId(messageId: String) : RuntimeException("There is no any message with $messageId id")
