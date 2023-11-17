package ir.erfansn.nsmavpn.ui.util

import java.text.DecimalFormat

fun Long?.toHumanReadableByteSize(): String = when {
    this == null -> "--- Bytes"
    this < 0 -> throw IllegalArgumentException("Invalid file size: $this")
    this < 1024 -> "$this Bytes"
    else -> {
        val unitIdx = (63 - java.lang.Long.numberOfLeadingZeros(this)) / 10
        format(1L shl unitIdx * 10, "${"KMGTPE"[unitIdx - 1]}iB")
    }
}

private fun Long.format(divider: Long, unitName: String): String {
    return DecimalFormat("#.##").format(this / divider.toDouble()) + " " + unitName
}
