package ir.erfansn.nsmavpn.data.model

import ir.erfansn.nsmavpn.data.source.local.datastore.UserProfile

fun UserProfile.isEmpty(): Boolean {
    return avatarUrl.isEmpty() || emailAddress.isEmpty() || displayName.isEmpty()
}
