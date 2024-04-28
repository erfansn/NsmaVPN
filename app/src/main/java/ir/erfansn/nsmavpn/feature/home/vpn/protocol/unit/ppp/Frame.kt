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
package ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.debug.assertAlways
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.toIntAsUShort
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.DataUnit
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.sstp.SSTP_PACKET_TYPE_DATA
import java.nio.ByteBuffer

const val PPP_HEADER = 0xFF03.toShort()

const val PPP_PROTOCOL_LCP = 0xC021.toShort()
const val PPP_PROTOCOL_PAP = 0xC023.toShort()
const val PPP_PROTOCOL_CHAP = 0xC223.toShort()
const val PPP_PROTOCOL_IPCP = 0x8021.toShort()
const val PPP_PROTOCOL_IPv6CP = 0x8057.toShort()
const val PPP_PROTOCOL_IP = 0x0021.toShort()
const val PPP_PROTOCOL_IPv6 = 0x0057.toShort()

abstract class Frame : DataUnit {
    abstract val code: Byte
    abstract val protocol: Short

    private val offsetSize = 8 // from SSTP header to PPP protocol
    protected val headerSize = offsetSize + 4 // add code, id and frame length

    protected var givenLength = 0

    var id: Byte = 0

    protected fun readHeader(buffer: ByteBuffer) {
        assertAlways(buffer.short == SSTP_PACKET_TYPE_DATA)
        givenLength = buffer.short.toIntAsUShort()

        assertAlways(buffer.short == PPP_HEADER)
        assertAlways(buffer.short == protocol)
        assertAlways(buffer.get() == code)
        id = buffer.get()
        assertAlways(buffer.short.toIntAsUShort()+ offsetSize == givenLength)
    }

    protected fun writeHeader(buffer: ByteBuffer) {
        buffer.putShort(SSTP_PACKET_TYPE_DATA)
        buffer.putShort(length.toShort())

        buffer.putShort(PPP_HEADER)
        buffer.putShort(protocol)
        buffer.put(code)
        buffer.put(id)
        buffer.putShort((length - offsetSize).toShort())
    }
}
