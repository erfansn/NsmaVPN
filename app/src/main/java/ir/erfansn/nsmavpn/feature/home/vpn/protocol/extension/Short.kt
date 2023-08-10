package ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension

fun Short.toIntAsUShort(): Int {
    return this.toInt() and 0x0000FFFF
}
