package ir.erfansn.nsmavpn.ui.util

import java.text.DecimalFormat

fun Long.toHumanReadableByteSizeAndUnit(
    unitsName: Array<String> = arrayOf(
        "Bytes",
        "KiB",
        "MiB",
        "GiB",
        "TiB",
        "PiB",
        "EiB",
    )
): Pair<String, String> {
    require(unitsName.size == 7) { "Count of unit names must be exactly seven" }
    require(this >= 0) { "Invalid Byte size: $this" }

    val unitIdx = (63 - java.lang.Long.numberOfLeadingZeros(this)) / 10
    return DecimalFormat("#.##").format(toDouble() / (1L shl unitIdx * 10)) to unitsName[unitIdx]
}
