package ir.erfansn.nsmavpn.data.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class DefaultLinkAvailabilityChecker @Inject constructor(
    private val ioDispatcher: CoroutineDispatcher,
) : LinkAvailabilityChecker {

    override suspend fun checkLink(address: String): Boolean {
        val url = URL(address)
        return withContext(ioDispatcher) {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"
                connectTimeout = 5000

                connect()

                responseCode == HttpURLConnection.HTTP_OK
            }
        }
    }
}

interface LinkAvailabilityChecker {
    suspend fun checkLink(address: String): Boolean
}
