package ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit

import java.nio.ByteBuffer

interface DataUnit {
    val length: Int
    fun write(buffer: ByteBuffer)
    fun read(buffer: ByteBuffer)
}
