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
package ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.debug.assertAlways
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.probeByte
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.probeShort
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.toIntAsUShort
import java.nio.ByteBuffer

const val OPTION_TYPE_LCP_MRU: Byte = 1
const val OPTION_TYPE_LCP_AUTH: Byte = 3

const val AUTH_PROTOCOL_PAP = 0xC023.toShort()
const val AUTH_PROTOCOL_CHAP = 0xC223.toShort()

const val CHAP_ALGORITHM_MSCHAPv2 = 0x81.toByte()

class MRUOption : Option() {
    override val type = OPTION_TYPE_LCP_MRU
    override val length = headerSize + Short.SIZE_BYTES

    var unitSize = 0

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        unitSize = buffer.short.toIntAsUShort()
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        buffer.putShort(unitSize.toShort())
    }
}

abstract class AuthOption : Option() {
    override val type = OPTION_TYPE_LCP_AUTH
    abstract val protocol: Short

    override fun readHeader(buffer: ByteBuffer) {
        super.readHeader(buffer)
        assertAlways(buffer.short == protocol)
    }

    override fun writeHeader(buffer: ByteBuffer) {
        super.writeHeader(buffer)
        buffer.putShort(protocol)
    }
}

class AuthOptionPAP : AuthOption() {
    override val protocol = AUTH_PROTOCOL_PAP
    override val length = headerSize + Short.SIZE_BYTES

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)
    }
}

class AuthOptionMSChapv2 : AuthOption() {
    override val protocol = AUTH_PROTOCOL_CHAP
    override val length = headerSize + Short.SIZE_BYTES + Byte.SIZE_BYTES
    val algorithm: Byte = CHAP_ALGORITHM_MSCHAPv2

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        assertAlways(buffer.get() == algorithm)
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        buffer.put(algorithm)
    }
}

class AuthOptionUnknown(override val protocol: Short) : AuthOption() {
    override val length: Int
        get() = headerSize + Short.SIZE_BYTES + holder.size

    var holder = ByteArray(0)

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        val holderSize = givenLength - length
        assertAlways(holderSize >= 0)

        if (holderSize > 0) {
            holder = ByteArray(holderSize).also { buffer.get(it) }
        }
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        buffer.put(holder)
    }
}

class LCPOptionPack(givenLength: Int = 0) : OptionPack(givenLength) {
    var mruOption: MRUOption? = null
    var authOption: AuthOption? = null

    override val knownOptions: List<Option>
        get() = mutableListOf<Option>().also { options ->
            mruOption?.also { options.add(it) }
            authOption?.also { options.add(it) }
        }

    override fun retrieveOption(buffer: ByteBuffer): Option {
        val option = when (val type = buffer.probeByte(0)) {
            OPTION_TYPE_LCP_MRU -> MRUOption().also { mruOption = it }

            OPTION_TYPE_LCP_AUTH -> {
                when (val protocol = buffer.probeShort(2)) {
                    AUTH_PROTOCOL_PAP -> AuthOptionPAP()
                    AUTH_PROTOCOL_CHAP -> {
                        if (buffer.probeByte(4) == CHAP_ALGORITHM_MSCHAPv2) {
                            AuthOptionMSChapv2()
                        } else {
                            AuthOptionUnknown(protocol)
                        }
                    }
                    else -> AuthOptionUnknown(protocol)
                }.also {
                    authOption = it
                }
            }

            else -> UnknownOption(type)
        }

        option.read(buffer)

        return  option
    }
}
