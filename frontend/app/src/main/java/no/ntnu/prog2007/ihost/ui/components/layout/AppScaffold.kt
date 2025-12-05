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
 */
@Composable
fun AppScaffold(
    navController: NavHostController,
    navigationState: NavigationState,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    stripeViewModel: StripeViewModel,
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
                stripeViewModel = stripeViewModel,
                friendViewModel = friendViewModel,
                userViewModel = userViewModel,
                modifier = Modifier.padding(padding),
                startDestination = startDestination
            )
        }
    }
}
