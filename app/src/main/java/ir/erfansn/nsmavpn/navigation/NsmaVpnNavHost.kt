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
package ir.erfansn.nsmavpn.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.services.gmail.GmailScopes
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.util.NetworkMonitor
import ir.erfansn.nsmavpn.feature.auth.AuthRoute
import ir.erfansn.nsmavpn.feature.auth.google.AuthenticationStatus
import ir.erfansn.nsmavpn.feature.auth.google.GoogleAuthState
import ir.erfansn.nsmavpn.feature.auth.google.rememberGoogleAuthState
import ir.erfansn.nsmavpn.feature.home.HomeRoute
import ir.erfansn.nsmavpn.feature.profile.ProfileRoute
import ir.erfansn.nsmavpn.feature.settings.settingsNavGraph
import ir.erfansn.nsmavpn.ui.util.whenResumed

@Composable
fun NsmaVpnNavHost(
    networkMonitor: NetworkMonitor,
    windowSize: WindowSizeClass,
    onResetApp: () -> Unit,
    isCompletedAuthFlow: suspend () -> Boolean,
    googleAuthState: GoogleAuthState = rememberGoogleAuthState(
        clientId = stringResource(R.string.web_client_id),
        Scope(GmailScopes.GMAIL_READONLY),
        Scope(Scopes.PROFILE),
        Scope(Scopes.EMAIL),
    ),
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = NavScreensRoute.Home,
    ) {
        composable(
            route = NavScreensRoute.Home,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "nsmavpn://{flag}"
                }
            )
        ) {
            var shouldShowHomeRoute by remember { mutableStateOf(false) }
            if (shouldShowHomeRoute) {
                HomeRoute(
                    viewModel = hiltViewModel(),
                    networkMonitor = networkMonitor,
                    windowSize = windowSize,
                    onNavigateToProfile = whenResumed {
                        navController.navigate(NavScreensRoute.Profile)
                    },
                    onNavigateToSettings = whenResumed {
                        navController.navigate(NavScreensRoute.Settings)
                    },
                    onRequestToReauthentication = whenResumed {
                        onResetApp()
                        navController.navigateToAuth()
                    },
                    hasNeedToReauth = googleAuthState.authStatus == AuthenticationStatus.PermissionsNotGranted,
                )
            }

            LaunchedEffect(isCompletedAuthFlow, navController, it) {
                if (it.arguments?.getString("flag") == "ir.erfansn.nsmavpn.MacroBenchmark") {
                    shouldShowHomeRoute = true
                    return@LaunchedEffect
                }

                if (!isCompletedAuthFlow()) {
                    navController.navigateToAuth()
                } else {
                    shouldShowHomeRoute = true
                }
            }
        }

        composable(NavScreensRoute.Auth) {
            AuthRoute(
                viewModel = hiltViewModel(),
                windowSize = windowSize,
                googleAuthState = googleAuthState,
                onNavigateToHome = whenResumed {
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
                onNavigateToBack = whenResumed(onState = navController::popBackStack),
                onSignOut = whenResumed {
                    onResetApp()
                    googleAuthState.signOut()
                    navController.navigateToAuth()
                }
            )
        }

        settingsNavGraph(
            navController = navController,
            windowSize = windowSize,
        )
    }
}
