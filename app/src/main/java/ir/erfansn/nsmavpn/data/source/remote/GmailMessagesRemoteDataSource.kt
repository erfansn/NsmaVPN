package ir.erfansn.nsmavpn.data.source.remote

import com.google.api.services.gmail.model.MessagePartBody
import ir.erfansn.nsmavpn.data.source.remote.api.GmailApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultGmailMessagesRemoteDataSource @Inject constructor(
    private val api: GmailApi,
    private val ioDispatcher: CoroutineDispatcher
) : GmailMessagesRemoteDataSource {

    override suspend fun fetchLatestMessageBodyText(userId: String, from: String) = withContext(ioDispatcher) {
        val data = api
            .selectAccount(userId)
            .users()
            .messages()
            .list("me")
            .setMaxResults(1)
            .setQ("from:$from")
            .execute()

        val firstMessage = data.messages.singleOrNull()
        firstMessage?.payload?.body?.decodeData()?.decodeToString()
    }
}

interface GmailMessagesRemoteDataSource {
    suspend fun fetchLatestMessageBodyText(userId: String, from: String): String?
}
