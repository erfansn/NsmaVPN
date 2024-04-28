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
