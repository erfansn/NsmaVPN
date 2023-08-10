package ir.erfansn.nsmavpn.feature.home.vpn.protocol.debug

class ParsingDataUnitException : Exception("Failed to parse data unit")

fun assertAlways(value: Boolean) {
    if (!value) {
        throw AssertionError()
    }
}
