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
package ir.erfansn.nsmavpn.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import ir.erfansn.nsmavpn.allowNotifications
import ir.erfansn.nsmavpn.startActivityInHomeRoute
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeBaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
                ?: throw Exception("targetAppId not passed as instrumentation runner arg"),
        ) {
            pressHome()
            allowNotifications()
            startActivityInHomeRoute()

            turnOnVpnJourneys()
        }
    }

    private fun MacrobenchmarkScope.turnOnVpnJourneys() {
        // Touch on vpn switch to turn it on
        device.findObject(By.enabled(true).res("vpn_switch_thumb")).click()
        device.waitForIdle()

        // Give the vpn connection permission (e.g. touching on OK button)
        device.wait(Until.findObject(By.res("android", "button1")), 1_000)?.click()

        // Wait until the state changes
        device.waitForIdle()
    }
}
