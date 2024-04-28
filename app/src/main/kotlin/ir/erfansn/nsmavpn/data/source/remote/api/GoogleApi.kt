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

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import javax.inject.Inject

abstract class GoogleApi<T : AbstractGoogleJsonClient> {

    protected abstract val credential: GoogleAccountCredential
    @Inject lateinit var httpTransport: NetHttpTransport
    @Inject lateinit var gsonFactory: GsonFactory

    protected abstract fun getService(): T

    fun selectAccount(emailAddress: String): T {
        credential.selectedAccountName = emailAddress
        checkNotNull(credential.selectedAccount) { "No account associate with $emailAddress in your device" }
        return getService()
    }
}
