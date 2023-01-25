package ir.erfansn.nsmavpn.data.model

import com.google.api.services.people.v1.model.Person

data class Profile(
    val avatarUrl: String?,
    val displayName: String,
    val emailAddress: String,
)

fun Person.toProfile() = Profile(
    avatarUrl = photos?.first()?.url,
    displayName = names?.first()?.displayName.orEmpty(),
    emailAddress = emailAddresses?.first()?.value.orEmpty().lowercase()
)
