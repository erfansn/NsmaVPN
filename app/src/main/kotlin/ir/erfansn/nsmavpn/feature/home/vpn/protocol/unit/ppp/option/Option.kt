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
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.toIntAsUByte
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.DataUnit
import java.nio.ByteBuffer

abstract class Option : DataUnit {
    abstract val type: Byte

    protected val headerSize = 2
    protected var givenLength = 0

    protected open fun readHeader(buffer: ByteBuffer) {
        assertAlways(type == buffer.get())
        givenLength = buffer.get().toIntAsUByte()
    }

    protected open fun writeHeader(buffer: ByteBuffer) {
        buffer.put(type)
        buffer.put(length.toByte())
    }
}

class UnknownOption(unknownType: Byte) : Option() {
    override val type = unknownType
    override val length: Int
        get() = headerSize + holder.size

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

abstract class OptionPack(private val givenLength: Int) : DataUnit {
    abstract val knownOptions: List<Option>
    var unknownOptions = listOf<UnknownOption>()
    val allOptions: List<Option>
        get() = knownOptions + unknownOptions

    var order: MutableMap<Byte, Int> = mutableMapOf()

    override val length: Int
        get() = allOptions.fold(0) {sum, option -> sum + option.length}

    protected abstract fun retrieveOption(buffer: ByteBuffer): Option

    private fun ensureValidOrder() {
        var nextIndex = order.values.maxOrNull() ?: 0

        allOptions.forEach {
            if (!order.containsKey(it.type)) {
                order[it.type] = nextIndex
                nextIndex++
            }
        }
    }

    override fun read(buffer: ByteBuffer) {
        var remaining = givenLength - length

        val currentOrder = mutableMapOf<Byte, Int>()
        val currentUnknownOptions = mutableListOf<UnknownOption>()

        var i = 0
        while (true) {
            assertAlways(remaining >= 0)
            if (remaining == 0) {
                break
            }

            retrieveOption(buffer).also {
                // if the option type is duplicated, the last option is preferred now
                currentOrder[it.type] = i
                remaining -= it.length

                if (it is UnknownOption) {
                    currentUnknownOptions.add(it)
                }
             }

            i++
        }

        order = currentOrder
        unknownOptions = currentUnknownOptions.toList()
    }

    override fun write(buffer: ByteBuffer) {
        ensureValidOrder()

        allOptions.sortedBy { option -> order[option.type] }.forEach { it.write(buffer) }
    }
}
