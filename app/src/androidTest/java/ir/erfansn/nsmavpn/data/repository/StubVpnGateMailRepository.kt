package ir.erfansn.nsmavpn.data.repository

import java.net.URL
import javax.inject.Inject

class StubVpnGateMailRepository @Inject constructor() : VpnGateMailRepository {

    override suspend fun isSubscribedToDailyMail(emailAddress: String): Boolean {
        return true
    }

    override suspend fun obtainMirrorSiteAddresses(emailAddress: String, lastDay: Int): List<URL> {
        return emptyList()
    }
}
