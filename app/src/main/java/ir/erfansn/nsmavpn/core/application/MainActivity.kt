package ir.erfansn.nsmavpn.core.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.erfansn.nsmavpn.feature.home.HomeRoute
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme

@ExperimentalMaterial3WindowSizeClassApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            NsmaVpnTheme {
                NsmaVpnBackground {
                    val windowSize = calculateWindowSizeClass(activity = this@MainActivity)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        /*ProfileRoute(
                            internetNetworkMonitor = internetNetworkMonitor,
                            windowSize = windowSize,
                        )
                        AuthRoute(
                            internetNetworkMonitor = internetNetworkMonitor,
                            windowSize = windowSize,
                            onNavigateToHome = { },
                        )
                        SettingsRoute(
                            onDismiss = { }
                        )*/
                        HomeRoute(
                            /*internetNetworkMonitor = networkMonitor,*/
                            windowSize = windowSize,
                        )
                    }
                }
            }
        }
    }
}
