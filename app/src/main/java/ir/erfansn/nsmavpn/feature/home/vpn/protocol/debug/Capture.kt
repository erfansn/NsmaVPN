package ir.erfansn.nsmavpn.feature.home.vpn.protocol.debug

import android.util.Log
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.DEFAULT_MRU
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.toHexString
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.DataUnit
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

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
