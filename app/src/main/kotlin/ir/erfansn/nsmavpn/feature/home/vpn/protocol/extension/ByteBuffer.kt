/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

fun ByteBuffer.slide() {
    val remaining = this.remaining()

    this.array().also {
        it.copyInto(it, 0, this.position(), this.limit())
    }

    this.position(0)
    this.limit(remaining)
}
