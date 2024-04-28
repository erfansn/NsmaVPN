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
package ir.erfansn.nsmavpn.feature.home.vpn.protocol.debug

import android.util.Log
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.DEFAULT_MRU
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.toHexString
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.DataUnit
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Direction {
    RECEIVED,
    SENT,
}

class DataUnitCapture {
    private val mutex = Mutex()
    private val buffer = ByteBuffer.allocate(DEFAULT_MRU)
    private val currentTime: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

    suspend fun logDataUnit(unit: DataUnit, direction: Direction) = mutex.withLock<Unit> {
        val message = mutableListOf(direction.name)

        message.add("[INFO]")
        message.add("time = $currentTime")
        message.add("size = ${unit.length}")
        message.add("class = ${unit::class.java.simpleName}")
        message.add("")

        message.add("[HEX]")
        buffer.clear()
        unit.write(buffer)
        buffer.flip()
        message.add(buffer.array().sliceArray(0 until unit.length).toHexString(true))
        message.add("")

        message.reduce { acc, s -> acc + "\n" + s }.also {
            Log.d("CAPTURE", it)
        }
    }
}
