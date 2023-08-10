package ir.erfansn.nsmavpn.data.model

data class DataUsage(
    val received: Long = 0L,
    val transmitted: Long = 0L,
)

operator fun DataUsage.minus(other: DataUsage): DataUsage {
    return DataUsage(
        received = received - other.received,
        transmitted = transmitted - other.transmitted,
    )
}
