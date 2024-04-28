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
package ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.debug.assertAlways
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.move
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.padZeroByte
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.toIntAsUShort
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.DataUnit
import java.nio.ByteBuffer

const val SSTP_ATTRIBUTE_ID_NO_ERROR: Byte = 0
const val SSTP_ATTRIBUTE_ID_ENCAPSULATED_PROTOCOL_ID: Byte = 1
const val SSTP_ATTRIBUTE_ID_STATUS_INFO: Byte = 2
const val SSTP_ATTRIBUTE_ID_CRYPTO_BINDING: Byte = 3
const val SSTP_ATTRIBUTE_ID_CRYPTO_BINDING_REQ: Byte = 4

const val CERT_HASH_PROTOCOL_SHA1: Byte = 1
const val CERT_HASH_PROTOCOL_SHA256: Byte = 2

abstract class Attribute : DataUnit {
    abstract val id: Byte
    protected var givenLength = 0

    protected fun readHeader(buffer: ByteBuffer) {
        buffer.move(1)
        assertAlways(buffer.get() == id)
        givenLength = buffer.short.toIntAsUShort()
    }

    protected fun writeHeader(buffer: ByteBuffer) {
        buffer.put(0)
        buffer.put(id)
        buffer.putShort(length.toShort())
    }
}

class EncapsulatedProtocolId : Attribute() {
    override val id = SSTP_ATTRIBUTE_ID_ENCAPSULATED_PROTOCOL_ID
    override val length = 6

    var protocolId: Short = 1

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)
        assertAlways(givenLength == length)

        protocolId = buffer.short
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        buffer.putShort(protocolId)
    }
}

class StatusInfo : Attribute() {
    override val id = SSTP_ATTRIBUTE_ID_STATUS_INFO

    override val length: Int
        get() = minimumLength + holder.size

    private val minimumLength = 12
    private val maximumHolderSize = 64

    var targetId: Byte = 0

    var status: Int = 0

    var holder = ByteArray(0)

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)
        val holderSize = givenLength - minimumLength
        assertAlways(holderSize in 0..maximumHolderSize)

        buffer.move(3)
        targetId = buffer.get()
        status = buffer.int
        holder = ByteArray(holderSize).also {
            buffer.get(it)
        }
    }

    override fun write(buffer: ByteBuffer) {
        assertAlways(holder.size <= maximumHolderSize)

        writeHeader(buffer)
        buffer.padZeroByte(3)
        buffer.put(targetId)
        buffer.putInt(status)
        buffer.put(holder)
    }
}

class CryptoBinding : Attribute() {
    override val id = SSTP_ATTRIBUTE_ID_CRYPTO_BINDING
    override val length = 104

    var hashProtocol: Byte = 2

    val nonce = ByteArray(32)

    val certHash = ByteArray(32)

    val compoundMac = ByteArray(32)

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)
        assertAlways(givenLength == length)

        buffer.move(3)
        hashProtocol = buffer.get()
        buffer.get(nonce)
        buffer.get(certHash)
        buffer.get(compoundMac)
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)
        buffer.padZeroByte(3)
        buffer.put(hashProtocol)
        buffer.put(nonce)
        buffer.put(certHash)
        buffer.put(compoundMac)
    }
}

class CryptoBindingRequest : Attribute() {
    override val id = SSTP_ATTRIBUTE_ID_CRYPTO_BINDING_REQ
    override val length = 40

    var bitmask: Byte = 3

    val nonce = ByteArray(32)

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)
        assertAlways(givenLength == length)

        buffer.move(3)
        bitmask = buffer.get()
        buffer.get(nonce)
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        buffer.padZeroByte(3)
        buffer.put(bitmask)
        buffer.put(nonce)
    }
}
