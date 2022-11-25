package ir.erfansn.nsmavpn.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

suspend fun <T, R : Comparable<R>> Iterable<T>.asyncMinByOrNull(coroutineScope: CoroutineScope, selector: suspend (T) -> R): T? {
    val result = map {
        coroutineScope.async {
            selector(it) to it
        }
    }.awaitAll()

    return result.minByOrNull(Pair<R, T>::first)?.second
}
