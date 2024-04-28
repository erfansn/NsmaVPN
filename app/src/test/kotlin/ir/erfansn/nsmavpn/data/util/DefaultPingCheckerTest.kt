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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultPingCheckerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val defaultPingChecker = DefaultPingChecker(testDispatcher)

    @Test
    fun returnsNotAvailableConstant_whenMeasuringAnEmptyHostname() =
        runTest(testDispatcher.scheduler) {
            val result = defaultPingChecker.measure("")
            assertThat(result).isEqualTo(PingChecker.NOT_AVAILABLE)
        }

    @Test
    fun returnsNotAvailableConstant_whenMeasuringAnInvalidHostname() =
        runTest(testDispatcher.scheduler, timeout = Duration.ZERO) {
            val result = defaultPingChecker.measure("com.google")
            assertThat(result).isEqualTo(PingChecker.NOT_AVAILABLE)
        }

    @Test
    fun returnsPositiveAmount_whenMeasureAnAvailableServer() = runTest(testDispatcher.scheduler) {
        val result = defaultPingChecker.measure("google.com")
        assertThat(result).isNotEqualTo(PingChecker.NOT_AVAILABLE)
    }

    @Test
    fun indicatesNotReachableHostName_whenIsEmpty() = runTest(testDispatcher.scheduler) {
        val result = defaultPingChecker.isReachable("")
        assertThat(result).isFalse()
    }

    @Test
    fun indicatesNotReachableHostName_whenIsInvalid() = runTest(testDispatcher.scheduler) {
        val result = defaultPingChecker.isReachable("com.google")
        assertThat(result).isFalse()
    }

    @Test
    fun indicatesReachableHostName_whenIsValid() = runTest(testDispatcher.scheduler) {
        val result = defaultPingChecker.isReachable("google.com")
        assertThat(result).isTrue()
    }
}