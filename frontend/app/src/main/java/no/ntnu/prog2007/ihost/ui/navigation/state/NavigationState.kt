package no.ntnu.prog2007.ihost.ui.navigation.state

import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import no.ntnu.prog2007.ihost.ui.navigation.config.Destination

/**
 * Navigation state holder
 */
@Composable
fun rememberNavigationState(navController: NavController): NavigationState {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    return remember(currentRoute) {
        NavigationState(
            currentRoute = currentRoute,
            navController = navController
        )
    }
}

data class NavigationState(
    val currentRoute: String?,
    private val navController: NavController
) {
    val shouldShowBottomNav: Boolean
        get() = currentRoute in bottomNavScreens.map { it.route }

    val shouldShowHeader: Boolean
        get() = currentRoute in headerScreens.map { it.route }

    val isOnAuthScreen: Boolean
        get() = currentRoute in authScreens

    fun navigateToEvents() {
        navController.navigate(Destination.Events.route) {
            popUpTo(Destination.Welcome.route) { inclusive = true }
        }
    }

    fun navigateToWelcome() {
        navController.navigate(Destination.Welcome.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    companion object {
        private val bottomNavScreens = listOf(
            Destination.Events,
            Destination.AddEvent,
            Destination.Profile
        )

        private val headerScreens = listOf(
            Destination.Events,
            Destination.AddEvent,
            Destination.Profile
        )

        private val authScreens = listOf(
            Destination.Welcome.route,
            Destination.Login.route,
            Destination.SignUp.route,
            Destination.PersonalInfo.route,
            Destination.ForgotPassword.route
        )
    }
}
