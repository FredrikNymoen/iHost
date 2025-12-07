package no.ntnu.prog2007.ihost.ui.navigation.state

import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import no.ntnu.prog2007.ihost.ui.navigation.config.Destination

/**
 * Creates and remembers a NavigationState instance
 *
 * Factory function that creates a NavigationState tied to the current
 * route from the NavController. Updates automatically when route changes.
 *
 * @param navController Navigation controller to observe
 * @return NavigationState instance
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

/**
 * Navigation state holder with UI configuration
 *
 * Encapsulates navigation state and provides utility properties for
 * determining UI visibility (bottom nav, header) based on current route.
 * Also provides type-safe navigation functions for common transitions.
 *
 * @property currentRoute Current active route string
 * @property navController Navigation controller for navigation actions
 */
data class NavigationState(
    val currentRoute: String?,
    private val navController: NavController
) {
    /**
     * Whether bottom navigation bar should be shown for current route
     */
    val shouldShowBottomNav: Boolean
        get() = currentRoute in bottomNavScreens.map { it.route }

    /**
     * Whether app header should be shown for current route
     */
    val shouldShowHeader: Boolean
        get() = currentRoute in headerScreens.map { it.route }

    /**
     * Whether current route is an authentication screen
     */
    val isOnAuthScreen: Boolean
        get() = currentRoute in authScreens

    /**
     * Navigate to Events screen and clear auth screens from back stack
     */
    fun navigateToEvents() {
        navController.navigate(Destination.Events.route) {
            popUpTo(Destination.Welcome.route) { inclusive = true }
        }
    }

    /**
     * Navigate to Welcome screen and clear entire back stack
     */
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
