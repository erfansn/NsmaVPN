package ir.erfansn.nsmavpn.navigation

import androidx.navigation.NavController

fun NavController.navigateToAuth() {
    navigate(NavScreensRoute.Auth) {
        popUpTo(NavScreensRoute.Home) {
            inclusive = true
        }
    }
}

object NavScreensRoute {
    const val Home = "home"
    const val Auth = "auth"
    const val Profile = "profile"
    const val Settings = "settings"
}
