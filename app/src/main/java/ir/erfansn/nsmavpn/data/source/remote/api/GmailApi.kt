package ir.erfansn.nsmavpn.data.source.remote.api

import android.content.Context
import com.google.api.services.gmail.Gmail
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.erfansn.nsmavpn.R
import javax.inject.Inject

class GmailApi @Inject constructor(
    @ApplicationContext private val context: Context,
) : GoogleApi<Gmail>() {

    override fun getService(): Gmail =
        Gmail.Builder(httpTransport, gsonFactory, credential)
            .setApplicationName(context.getString(R.string.app_name))
            .build()
}
