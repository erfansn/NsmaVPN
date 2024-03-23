package ir.erfansn.nsmavpn.data.source.remote.api

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.erfansn.nsmavpn.R
import javax.inject.Inject

class GmailApi @Inject constructor(
    @ApplicationContext private val context: Context,
) : GoogleApi<Gmail>() {

    override val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(
        context,
        listOf(GmailScopes.GMAIL_READONLY),
    )

    override fun getService(): Gmail =
        Gmail.Builder(httpTransport, gsonFactory, credential)
            .setApplicationName(ContextCompat.getString(context, R.string.app_name))
            .build()
}
