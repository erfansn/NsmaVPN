package ir.erfansn.nsmavpn.feature.settings

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import ir.erfansn.nsmavpn.feature.settings.tunnelsplitting.TunnelSplittingRoute
import ir.erfansn.nsmavpn.navigation.NavScreensRoute
import ir.erfansn.nsmavpn.ui.util.whenResumed

fun NavGraphBuilder.settingsNavGraph(
    windowSize: WindowSizeClass,
    navController: NavController,
) {
    navigation(
        route = NavScreensRoute.Settings,
        startDestination = NavSettingsRoute.Main,
    ) {
        composable(NavSettingsRoute.Main) {
            SettingsRoute(
                viewModel = hiltViewModel(),
                onNavigateToBack = whenResumed(onState = navController::popBackStack),
                onNavigateToTunnelSplitting = whenResumed {
                    navController.navigate(NavSettingsRoute.TunnelSplitting)
                }
            )
        }

        composable(NavSettingsRoute.TunnelSplitting) {
            TunnelSplittingRoute(
                viewModel = hiltViewModel(),
                windowClass = windowSize,
                onNavigateToBack = whenResumed(onState = navController::popBackStack)
            )
        }
    }
}

private object NavSettingsRoute {
    const val Main = "main"
    const val TunnelSplitting = "tunnel_splitting"
}
