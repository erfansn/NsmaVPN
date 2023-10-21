package ir.erfansn.nsmavpn.data.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

suspend fun <T, R> T.runWithContext(
    coroutineContext: CoroutineContext,
    block: suspend T.() -> R,
): R {
    return withContext(coroutineContext) { block() }
}

suspend inline fun <T, R : Comparable<R>> Iterable<T>.asyncMinByOrNull(crossinline selector: suspend (T) -> R) =
    asyncMap {
        selector(it) to it
    }.minByOrNull {
        it.first
    }?.second

suspend inline fun <T> Iterable<T>.asyncPartition(
    crossinline selector: suspend (T) -> Boolean,
): Pair<List<T>, List<T>> {
    val first = ArrayList<T>()
    val second = ArrayList<T>()

    asyncMap {
        selector(it) to it
    }.fastForEach { (predicate, value) ->
        if (predicate) {
            first.add(value)
        } else {
            second.add(value)
        }
    }
    return Pair(first, second)
}

/**
 * Use this for walk over random-access collections
 * it's free from extra allocation of Iteration objects thus provides better performance.
 *
 * [See this video](https://youtu.be/MZOf3pOAM6A?si=C2GdYPiCCW38eoqU)
 **/
@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) action(this[index])
}

suspend inline fun <T> Iterable<T>.asyncFilterNotTo(crossinline predicate: suspend (T) -> Boolean) =
    asyncMap {
        predicate(it) to it
    }.filter {
        it.first
    }.map {
        it.second
    }

suspend inline fun <T> Iterable<T>.asyncFirstOrNull(crossinline predicate: suspend (T) -> Boolean) =
    asyncMap {
        predicate(it) to it
    }.firstOrNull {
        it.first
    }?.second

suspend inline fun <T, R> Iterable<T>.asyncMap(
    crossinline block: suspend (T) -> R,
): List<R> = coroutineScope {
    map {
        async {
            block(it)
        }
    }.awaitAll()
}
