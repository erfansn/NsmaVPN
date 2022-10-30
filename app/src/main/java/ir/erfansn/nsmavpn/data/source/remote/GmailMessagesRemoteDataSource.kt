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

    override suspend fun fetchFirstMessageBody(userName: String, fromGmail: String) = withContext(ioDispatcher) {
        val data = api
            .selectAccount(userName)
            .users()
            .messages()
            .list("me")
            .setQ("from:$fromGmail")
            .execute()

        val firstMessage = data.messages.firstOrNull()
        firstMessage?.payload?.body
    }
}

interface GmailMessagesRemoteDataSource {
    suspend fun fetchFirstMessageBody(userName: String, fromGmail: String): MessagePartBody?
}
