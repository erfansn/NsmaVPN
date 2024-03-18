package ir.erfansn.nsmavpn.data.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend inline fun <T, R : Comparable<R>> Iterable<T>.asyncMinByOrNull(crossinline selector: suspend (T) -> R) =
    asyncMap {
        selector(it) to it
    }.minByOrNull {
        it.first
    }?.second

suspend inline fun <T> Iterable<T>.asyncFilter(crossinline predicate: suspend (T) -> Boolean) =
    asyncMap {
        predicate(it) to it
    }.filter {
        it.first
    }.map {
        it.second
    }

suspend inline fun <T, R> Iterable<T>.asyncMap(
    crossinline block: suspend (T) -> R,
): List<R> = coroutineScope {
    map {
        async {
            block(it)
        }
    }.awaitAll()
}
