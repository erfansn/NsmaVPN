package ir.erfansn.nsmavpn.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ir.erfansn.nsmavpn.core.application.AppViewModel
import ir.erfansn.nsmavpn.data.util.NetworkMonitor
import ir.erfansn.nsmavpn.feature.auth.AuthRoute
import ir.erfansn.nsmavpn.feature.home.HomeRoute
import ir.erfansn.nsmavpn.feature.profile.ProfileRoute
import ir.erfansn.nsmavpn.feature.settings.settingsNavGraph
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun NsmaVpnNavGraph(
    appViewModel: AppViewModel,
    networkMonitor: NetworkMonitor,
    windowSize: WindowSizeClass,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = NavScreensRoute.Home,
    ) {
        composable(NavScreensRoute.Home) {
            var isLoggedIn by remember { mutableStateOf(false) }
            if (isLoggedIn) {
                HomeRoute(
                    viewModel = hiltViewModel(),
                    networkMonitor = networkMonitor,
                    windowSize = windowSize,
                    onNavigateToProfile = {
                        navController.navigate(NavScreensRoute.Profile)
                    },
                    onNavigateToSettings = {
                        navController.navigate(NavScreensRoute.Settings)
                    },
                )
            }

            LaunchedEffect(appViewModel, navController) {
                appViewModel
                    .loggedInState
                    .onEach {
                        if (!it) {
                            navController.navigateToAuth()
                        }
                        isLoggedIn = it
                    }
                    .launchIn(this)
            }
        }

        composable(NavScreensRoute.Auth) {
            AuthRoute(
                viewModel = hiltViewModel(),
                windowSize = windowSize,
                onNavigateToHome = {
                    navController.navigate(NavScreensRoute.Home) {
                        popUpTo(NavScreensRoute.Auth) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(NavScreensRoute.Profile) {
            ProfileRoute(
                viewModel = hiltViewModel(),
                windowSize = windowSize,
                onNavigateToBack = navController::popBackToHome,
                onNavigateToAuth = navController::navigateToAuth
            )
        }

        settingsNavGraph(
            navController = navController,
            windowSize = windowSize,
        )
    }
}