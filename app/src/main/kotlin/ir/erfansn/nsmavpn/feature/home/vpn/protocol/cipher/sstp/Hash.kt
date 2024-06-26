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
package ir.erfansn.nsmavpn.feature.home.vpn.protocol.cipher.sstp

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.cipher.ppp.hashMd4
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ChapMessage
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.sum
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.toHexByteArray
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.CERT_HASH_PROTOCOL_SHA1
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.CERT_HASH_PROTOCOL_SHA256
import java.security.MessageDigest

class HashSetting(hashProtocol: Byte) {
    val cmacSize: Short // little endian
    val digestProtocol: String
    val macProtocol: String

    init {
        when (hashProtocol) {
            CERT_HASH_PROTOCOL_SHA1 -> {
                cmacSize = 0x1400.toShort()
                digestProtocol = "SHA-1"
                macProtocol = "HmacSHA1"

            }

            CERT_HASH_PROTOCOL_SHA256 -> {
                cmacSize = 0x2000.toShort()
                digestProtocol = "SHA-256"
                macProtocol = "HmacSHA256"
            }

            else -> throw NotImplementedError(hashProtocol.toString())
        }
    }
}

fun generateChapHLAK(password: String, chapMessage: ChapMessage): ByteArray {
    val passArray = password.toByteArray(Charsets.UTF_16LE)

    val magic1 = sum(
        "5468697320697320746865204D505045",
        "204D6173746572204B6579"
    ).toHexByteArray()

    val magic2 = sum(
        "4F6E2074686520636C69656E74207369",
        "64652C20746869732069732074686520",
        "73656E64206B65793B206F6E20746865",
        "2073657276657220736964652C206974",
        "20697320746865207265636569766520",
        "6B65792E"
    ).toHexByteArray()

    val magic3 = sum(
        "4F6E2074686520636C69656E74207369",
        "64652C20746869732069732074686520",
        "72656365697665206B65793B206F6E20",
        "7468652073657276657220736964652C",
        "206974206973207468652073656E6420",
        "6B65792E"
    ).toHexByteArray()

    val pad1 = sum(
        "00000000000000000000000000000000",
        "00000000000000000000000000000000",
        "0000000000000000"
    ).toHexByteArray()

    val pad2 = sum(
        "F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2",
        "F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2",
        "F2F2F2F2F2F2F2F2"
    ).toHexByteArray()

    return MessageDigest.getInstance("SHA-1").let {
        it.update(hashMd4(hashMd4(passArray)))
        it.update(chapMessage.clientResponse)
        it.update(magic1)
        val masterKey = it.digest().sliceArray(0 until 16)

        val hlak = ByteArray(32)
        it.reset()
        it.update(masterKey)
        it.update(pad1)
        it.update(magic2)
        it.update(pad2)
        it.digest().copyInto(hlak, endIndex = 16)

        it.reset()
        it.update(masterKey)
        it.update(pad1)
        it.update(magic3)
        it.update(pad2)
        it.digest().copyInto(hlak, 16, endIndex = 16)

        hlak
    }
}
