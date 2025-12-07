package no.ntnu.prog2007.ihost.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import no.ntnu.prog2007.ihost.ui.navigation.config.AppNavHost
import no.ntnu.prog2007.ihost.ui.navigation.config.Destination
import no.ntnu.prog2007.ihost.ui.navigation.state.NavigationState
import no.ntnu.prog2007.ihost.viewmodel.*

/**
 * Main app scaffold with gradient background, header, and bottom navigation
 *
 * Root composable for the authenticated app experience. Provides:
 * - Gradient background from theme colors
 * - Conditional top app header
 * - Conditional bottom navigation bar
 * - Navigation host for all screens
 *
 * @param navController Navigation controller for managing screen navigation
 * @param navigationState Current navigation state (route, header/nav visibility)
 * @param authViewModel ViewModel for authentication operations
 * @param eventViewModel ViewModel for event operations
 * @param friendViewModel ViewModel for friendship operations
 * @param userViewModel ViewModel for user profile operations
 * @param startDestination Initial destination route (Events or Welcome)
 */
@Composable
fun AppScaffold(
    navController: NavHostController,
    navigationState: NavigationState,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    friendViewModel: FriendViewModel,
    userViewModel: UserViewModel,
    startDestination: String
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface
        )
    )

    val bottomNavScreens = listOf(
        Destination.Events,
        Destination.AddEvent,
        Destination.Profile
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Scaffold(
            topBar = {
                if (navigationState.shouldShowHeader) {
                    AppHeader()
                }
            },
            bottomBar = {
                if (navigationState.shouldShowBottomNav) {
                    BottomNavigationBar(
                        navController = navController,
                        screens = bottomNavScreens,
                        currentRoute = navigationState.currentRoute
                    )
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            AppNavHost(
                navController = navController,
                authViewModel = authViewModel,
                eventViewModel = eventViewModel,
                friendViewModel = friendViewModel,
                userViewModel = userViewModel,
                modifier = Modifier.padding(padding),
                startDestination = startDestination
            )
        }
    }
}
