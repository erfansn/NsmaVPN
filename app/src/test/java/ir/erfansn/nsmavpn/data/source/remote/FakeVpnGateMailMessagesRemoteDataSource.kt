package ir.erfansn.nsmavpn.data.source.remote

import com.google.api.services.gmail.model.Message

class FakeVpnGateMailMessagesRemoteDataSource : VpnGateMailMessagesRemoteDataSource {

    lateinit var messages: Map<String, VpnGateMessage>

    override suspend fun fetchMessageIdList(emailAddress: String, lastDay: Int): List<String> {
        return messages.keys.toList()
    }

    override suspend fun fetchMessageBodyText(emailAddress: String, messageId: String): String {
        return messages[messageId]!!.content
    }

    override suspend fun fetchMessage(emailAddress: String, messageId: String): Message {
        return Message().setInternalDate(messages[messageId]!!.receiveTime)
    }
}

data class VpnGateMessage(
    val receiveTime: Long,
    val content: String,
)

