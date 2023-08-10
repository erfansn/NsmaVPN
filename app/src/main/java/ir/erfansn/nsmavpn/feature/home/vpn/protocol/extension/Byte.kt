package ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension

fun Byte.toIntAsUByte(): Int {
    return this.toInt() and 0x000000FF
}
