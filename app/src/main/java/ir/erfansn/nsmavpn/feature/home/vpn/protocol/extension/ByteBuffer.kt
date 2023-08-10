package ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension

import java.nio.ByteBuffer

fun ByteBuffer.move(diff: Int) {
    position(position() + diff)
}

fun ByteBuffer.padZeroByte(size: Int) {
    repeat(size) { put(0) }
}

fun ByteBuffer.probeByte(diff: Int): Byte {
    return this.get(this.position() + diff)
}

fun ByteBuffer.probeShort(diff: Int): Short {
    return this.getShort(this.position() + diff)
}

val ByteBuffer.capacityAfterLimit: Int
    get() = this.capacity() - this.limit()