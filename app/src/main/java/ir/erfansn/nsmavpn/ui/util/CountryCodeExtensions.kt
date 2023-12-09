package ir.erfansn.nsmavpn.ui.util

import ir.erfansn.nsmavpn.feature.home.vpn.CountryCode
import java.util.Locale

fun CountryCode.toCountryFlagEmoji() = value.uppercase()
    .fold(charArrayOf()) { acc, c ->
        acc + Character.toChars(c.code + 0x1F1A5)
    }
    .joinToString(separator = "")

fun CountryCode.toCountryName(): String =
    Locale("", value).displayCountry
