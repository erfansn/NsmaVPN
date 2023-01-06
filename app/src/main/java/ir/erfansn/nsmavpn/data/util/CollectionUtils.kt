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

suspend fun <T> Iterable<T>.asyncPartition(selector: suspend (T) -> Boolean): Pair<List<T>, List<T>> =
    coroutineScope {
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