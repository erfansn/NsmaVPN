package ir.erfansn.nsmavpn.data.util.monitor

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor<T> {
    val status: Flow<T>
}
