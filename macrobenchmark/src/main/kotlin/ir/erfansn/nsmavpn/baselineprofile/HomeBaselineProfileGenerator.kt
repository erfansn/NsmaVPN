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
