package ir.erfansn.nsmavpn.data.util

import android.net.TrafficStats
import ir.erfansn.nsmavpn.data.model.DataUsage
import ir.erfansn.nsmavpn.data.model.minus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

val totalDataUsage = flow {
    fun currentDataUsage() = DataUsage(
        transmitted = TrafficStats.getTotalTxBytes(),
        received = TrafficStats.getTotalRxBytes()
    )

    val initialDataUsage = currentDataUsage()
    while (true) {
        val currentDataUsage = currentDataUsage()
        emit(currentDataUsage - initialDataUsage)

        delay(1000)
    }
}.flowOn(Dispatchers.IO)
