package ir.erfansn.nsmavpn.data.source.remote.api

import android.content.Context
import com.google.api.services.people.v1.PeopleService
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.erfansn.nsmavpn.R
import javax.inject.Inject

class PeopleApi @Inject constructor(
    @ApplicationContext private val context: Context
) : GoogleApi<PeopleService>() {

    override fun getService(): PeopleService =
        PeopleService.Builder(httpTransport, gsonFactory, credential)
            .setApplicationName(context.getString(R.string.app_name))
            .build()
}
