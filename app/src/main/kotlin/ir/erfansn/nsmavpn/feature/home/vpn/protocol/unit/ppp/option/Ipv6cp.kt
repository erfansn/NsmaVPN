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

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.probeByte
import java.nio.ByteBuffer

const val OPTION_TYPE_IPv6CP_IDENTIFIER: Byte = 0x01

class Ipv6cpIdentifierOption : Option() {
    override val type = OPTION_TYPE_IPv6CP_IDENTIFIER
    val identifier = ByteArray(8)
    override val length = headerSize + identifier.size

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        buffer.get(identifier)
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        buffer.put(identifier)
    }
}

class Ipv6cpOptionPack(givenLength: Int = 0) : OptionPack(givenLength) {
    var identifierOption: Ipv6cpIdentifierOption? = null

    override val knownOptions: List<Option>
        get() = mutableListOf<Option>().also { options ->
            identifierOption?.also { options.add(it) }
        }

    override fun retrieveOption(buffer: ByteBuffer): Option {
        val option = when (val type = buffer.probeByte(0)) {
            OPTION_TYPE_IPv6CP_IDENTIFIER -> Ipv6cpIdentifierOption().also { identifierOption = it }

            else -> UnknownOption(type)
        }

        option.read(buffer)

        return option
    }
}
