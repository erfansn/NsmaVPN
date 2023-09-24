package ir.erfansn.nsmavpn.data.model

data class MirrorLink(
    val protocol: String,
    val hostName: String,
    val port: String,
) {
    override fun toString(): String {
        return "$protocol://$hostName:$port"
    }
}
