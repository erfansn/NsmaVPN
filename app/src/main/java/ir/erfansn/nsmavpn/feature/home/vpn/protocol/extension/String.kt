package ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension

fun sum(vararg words: String): String {
    var result = ""

    words.forEach {
        result += it
    }

    return result
}
