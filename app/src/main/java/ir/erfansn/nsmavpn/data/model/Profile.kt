package ir.erfansn.nsmavpn.data.model

data class Profile(
    val avatarUrl: String,
    val displayName: String,
    val emailAddress: String,
)

fun Profile.isEmpty(): Boolean {
    return emailAddress.isEmpty() || displayName.isEmpty() || avatarUrl.isEmpty()
}
