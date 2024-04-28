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

fun ByteArray.isSame(other: ByteArray): Boolean {
    if (this.size != other.size) return false

    this.zip(other).forEach {
        if (it.first != it.second) return false
    }

    return true
}

fun ByteArray.toHexString(parse: Boolean = false): String {
    var output = ""

    forEachIndexed { index, byte ->
        output += String.format("%02X", byte.toInt() and 0xFF)

        if (parse) output += if (index % 16 == 15) "\n" else " "
    }

    return output
}

fun String.toHexByteArray(): ByteArray {
    if (length % 2 != 0) throw Exception("Fragmented Byte")

    val arrayLength = length / 2
    val output = ByteArray(arrayLength)

    repeat(arrayLength) {
        val start = it * 2
        output[it] = this.slice(start..start + 1).toInt(16).toByte()
    }

    return output
}
