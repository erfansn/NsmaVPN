package ir.erfansn.nsmavpn.data.util

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

suspend fun <T, R> T.runWithContext(coroutineContext: CoroutineContext, block: suspend T.() -> R): R {
    return withContext(coroutineContext) { block() }
}

suspend inline fun <T, R : Comparable<R>> Iterable<T>.asyncMinByOrNull(
    crossinline selector: suspend (T) -> R,
): T? = coroutineScope {
    val result = map {
        async {
            selector(it) to it
        }
    }.awaitAll()

    result.minByOrNull(Pair<R, T>::first)?.second
}

suspend inline fun <T> Iterable<T>.asyncPartition(
    crossinline selector: suspend (T) -> Boolean,
): Pair<List<T>, List<T>> = coroutineScope {
    val result = map {
        async {
            selector(it) to it
        }
    }.awaitAll()

    val first = ArrayList<T>()
    val second = ArrayList<T>()
    for (element in result) {
        if (element.first) {
            first.add(element.second)
        } else {
            second.add(element.second)
        }
    }
    Pair(first, second)
}
