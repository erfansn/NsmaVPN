package ir.erfansn.nsmavpn.data.model

import ir.erfansn.nsmavpn.data.source.local.datastore.ProfileProto

data class Profile(
    val avatarUrl: String?,
    val displayName: String,
    val emailAddress: String,
)

fun Profile?.asProfileProto(): ProfileProto = this?.let {
    ProfileProto.newBuilder()
        .setEmailAddress(emailAddress)
        .setAvatarUrl(avatarUrl.orEmpty())
        .setDisplayName(displayName)
        .build()
} ?: ProfileProto.getDefaultInstance()
