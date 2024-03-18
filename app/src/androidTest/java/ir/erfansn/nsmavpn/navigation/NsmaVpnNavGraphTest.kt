package ir.erfansn.nsmavpn.navigation

import android.content.Context
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import ir.erfansn.nsmavpn.HiltTestActivity
import ir.erfansn.nsmavpn.data.util.StubNetworkMonitor
import ir.erfansn.nsmavpn.feature.auth.google.FakeGoogleAuthState
import ir.erfansn.nsmavpn.feature.auth.google.GoogleAuthState
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@HiltAndroidTest
@MediumTest
@RunWith(AndroidJUnit4::class)
class NsmaVpnNavGraphTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var navController: TestNavHostController

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class TestModule {
        @Binds
        abstract fun bindsGoogleAuthState(
            fakeGoogleAuthState: FakeGoogleAuthState
        ): GoogleAuthState
    }

    @Inject
    lateinit var googleAuthState: GoogleAuthState

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun startsWithAuthRoute_whenUserWasNotCompletedAuth() {
        composeTestRule.setContent {
            navController = rememberTestNavController()

            NsmaVpnNavHost(
                networkMonitor = StubNetworkMonitor,
                windowSize = calculateWindowSizeClass(composeTestRule.activity),
                isCompletedAuthFlow = { false },
                onResetApp = { },
                googleAuthState = googleAuthState,
                navController = navController
            )
        }

        assertThat(navController.currentDestination?.route).isEqualTo(NavScreensRoute.Auth)
    }

    @Test
    fun startsWithHomeRoute_whenUserWasCompletedAuthBefore() {
        composeTestRule.setContent {
            navController = rememberTestNavController()

            NsmaVpnNavHost(
                networkMonitor = StubNetworkMonitor,
                windowSize = calculateWindowSizeClass(composeTestRule.activity),
                isCompletedAuthFlow = { true },
                onResetApp = { },
                googleAuthState = googleAuthState,
                navController = navController
            )
        }

        assertThat(navController.currentDestination?.route).isEqualTo(NavScreensRoute.Home)
    }

    @Test
    fun navigateToHomeRoute_whenUserCompletedAuthFlow() {
        googleAuthState.signOut()

        composeTestRule.setContent {
            navController = rememberTestNavController()

            var isCompletedAuthFlow by remember { mutableStateOf(false) }
            LaunchedEffect(navController.currentDestination, googleAuthState) {
                if (navController.currentDestination?.route == NavScreensRoute.Auth) {
                    googleAuthState.signIn()
                    googleAuthState.requestPermissions()
                    isCompletedAuthFlow = true
                }
            }

            NsmaVpnNavHost(
                networkMonitor = StubNetworkMonitor,
                windowSize = calculateWindowSizeClass(composeTestRule.activity),
                isCompletedAuthFlow = {
                    delay(1.seconds)
                    isCompletedAuthFlow
                },
                onResetApp = { },
                googleAuthState = googleAuthState,
                navController = navController
            )
        }

        composeTestRule.waitForIdle()

        assertThat(navController.currentDestination?.route).isEqualTo(NavScreensRoute.Home)
    }

    @Composable
    private fun rememberTestNavController(context: Context = LocalContext.current): TestNavHostController {
        return remember {
            TestNavHostController(context)
                .apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
        }
    }
}
