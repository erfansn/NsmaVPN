package ir.erfansn.nsmavpn.navigation

import androidx.navigation.NavController

fun NavController.navigateToAuth() {
    navigate(NavScreensRoute.Auth) {
        popUpTo(NavScreensRoute.Home) {
            inclusive = true
        }
    }
}

fun NavController.popBackToHome() {
    // To prevent extra pop due of multiple call
    popBackStack(route = NavScreensRoute.Home, inclusive = false)
}

object NavScreensRoute {
    const val Home = "home"
    const val Auth = "auth"
    const val Profile = "profile"
    const val Settings = "settings"
}
