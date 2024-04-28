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
package ir.erfansn.nsmavpn.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NsmaBackgroundBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @OptIn(ExperimentalMetricApi::class)
    @Test
    fun backgroundImageWithAnimation() {
        benchmarkRule.measureRepeated(
            InstrumentationRegistry.getArguments().getString("targetAppId")
                ?: throw Exception("targetAppId not passed as instrumentation runner arg"),
            metrics = listOf(
                TraceSectionMetric("NsmaVpnBackgroundImageTrace"),
                FrameTimingMetric()
            ),
            compilationMode = CompilationMode.DEFAULT,
            startupMode = StartupMode.COLD,
            iterations = 10,
        ) {
            startActivityAndWait()
        }
    }
}
