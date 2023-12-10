package ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension

import android.net.Uri

fun sum(vararg words: String): String {
    var result = ""

    words.forEach {
        result += it
    }

    return result
}

fun String.toUri(): Uri? {
    return if (this.isEmpty()) {
        null
    } else {
        Uri.parse(this)
    }
}
