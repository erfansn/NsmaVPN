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

import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.ppp.option.IpcpOptionPack
import java.nio.ByteBuffer

abstract class IpcpFrame : Frame() {
    override val protocol = PPP_PROTOCOL_IPCP
}

abstract class IpcpConfigureFrame : IpcpFrame() {
    override val length: Int
        get() = headerSize + options.length

    var options: IpcpOptionPack = IpcpOptionPack()

    override fun read(buffer: ByteBuffer) {
        readHeader(buffer)

        options = IpcpOptionPack(givenLength - length).also {
            it.read(buffer)
        }
    }

    override fun write(buffer: ByteBuffer) {
        writeHeader(buffer)

        options.write(buffer)
    }
}

class IpcpConfigureRequest : IpcpConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_REQUEST
}

class IpcpConfigureAck : IpcpConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_ACK
}

class IpcpConfigureNak : IpcpConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_NAK
}

class IpcpConfigureReject : IpcpConfigureFrame() {
    override val code = LCP_CODE_CONFIGURE_REJECT
}
