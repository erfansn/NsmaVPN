/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
