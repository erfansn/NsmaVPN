package ir.erfansn.nsmavpn.data.model

import com.google.api.services.people.v1.model.Person
import ir.erfansn.nsmavpn.data.source.local.datastore.ProfileProto

data class Profile(
    val avatarUrl: String?,
    val displayName: String,
    val emailAddress: String,
)

fun Profile.asProfileProto(): ProfileProto = ProfileProto.newBuilder()
    .setEmailAddress(emailAddress)
    .setAvatarUrl(avatarUrl.orEmpty())
    .setDisplayName(displayName)
    .build()

fun Person.toProfile() = Profile(
    avatarUrl = photos?.first()?.url,
    displayName = names?.first()?.displayName.orEmpty(),
    emailAddress = emailAddresses?.first()?.value.orEmpty().lowercase()
)
