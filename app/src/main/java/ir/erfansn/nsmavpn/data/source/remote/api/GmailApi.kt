/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
