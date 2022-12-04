package ir.erfansn.nsmavpn.data.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <T, R : Comparable<R>> Iterable<T>.asyncMinByOrNull(selector: suspend (T) -> R): T? =
    coroutineScope {
        val result = map {
            async {
                selector(it) to it
            }
        }.awaitAll()

        result.minByOrNull(Pair<R, T>::first)?.second
    }
