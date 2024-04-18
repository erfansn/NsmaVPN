package ir.erfansn.nsmavpn.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthBaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
                ?: throw Exception("targetAppId not passed as instrumentation runner arg"),
        ) {
            pressHome()
            startActivityAndWait()

            pressSignInButtonThenBackJourneys()
        }
    }

    private fun MacrobenchmarkScope.pressSignInButtonThenBackJourneys() {
        // Touch on sign-in button
        device.findObject(By.res("sign_in")).click()

        // Wait until Google Sign-In dialog appears
        device.waitForWindowUpdate("com.google.android.gms", 5_000)
        device.waitForIdle()

        // Close the dialog
        device.pressBack()
        device.waitForIdle()
    }
}
