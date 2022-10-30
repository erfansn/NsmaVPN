package ir.erfansn.nsmavpn.data.source.remote.api

import com.google.api.services.gmail.Gmail

interface GmailApi {
    fun selectAccount(name: String): Gmail
}