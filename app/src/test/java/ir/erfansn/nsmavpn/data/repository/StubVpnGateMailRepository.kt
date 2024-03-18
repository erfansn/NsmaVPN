package ir.erfansn.nsmavpn.data.repository

import java.net.URL

class StubVpnGateMailRepository : VpnGateMailRepository {

    lateinit var mirrorLinks: List<URL>

    override suspend fun isSubscribedToDailyMail(emailAddress: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun obtainMirrorSiteAddresses(emailAddress: String, lastDay: Int): List<URL> {
        return if (lastDay > 10) {
            throw NoVpnGateSubscriptionException(emailAddress)
        } else {
            mirrorLinks
        }
    }
}