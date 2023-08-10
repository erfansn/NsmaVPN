package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.control

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class LogWriter(logOutput: OutputStream) {
    private val mutex = Mutex()
    private val outputStream = BufferedOutputStream(logOutput)
    private val currentTime: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

    fun write(message: String) {
        // use directly if it's cannot be race condition, like onCreate and onDestroy
        outputStream.write("[$currentTime] $message\n".toByteArray(Charsets.UTF_8))
    }

    suspend fun report(message: String) {
        mutex.withLock { write(message) }
    }

    fun close() {
        outputStream.flush()
        outputStream.close()
    }
}
