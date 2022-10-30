package ir.erfansn.nsmavpn.data.source.remote.api

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.erfansn.nsmavpn.R
import javax.inject.Inject

/**
 * A wrapper class to encapsulate [GoogleAccountCredential] and [Gmail] Api implementation
 *
 * @property context
 * @constructor Create [GmailApiImpl]
 */
class GmailApiImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GmailApi {
    private val credential = GoogleAccountCredential.usingOAuth2(context, listOf(GmailScopes.GMAIL_READONLY))

    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val gsonFactory = GsonFactory.getDefaultInstance()
    private val service = Gmail.Builder(httpTransport, gsonFactory, credential)
        .setApplicationName(context.getString(R.string.app_name))
        .build()

    override fun selectAccount(name: String): Gmail {
        credential.selectedAccountName = name
        return service
    }
}