package no.ntnu.prog2007.ihost.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import no.ntnu.prog2007.ihost.ui.screens.auth.LoginScreen
import no.ntnu.prog2007.ihost.ui.screens.auth.SignUpScreen
import no.ntnu.prog2007.ihost.ui.screens.events.EventsScreen
import no.ntnu.prog2007.ihost.ui.screens.addevent.AddEventScreen
import no.ntnu.prog2007.ihost.ui.screens.profile.ProfileScreen
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel

@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth screens
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Main app screens
        composable(Screen.Events.route) {
            EventsScreen(
                viewModel = eventViewModel,
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                }
            )
        }

        composable(Screen.AddEvent.route) {
            AddEventScreen(
                viewModel = eventViewModel,
                onEventCreated = {
                    navController.navigate(Screen.Events.route) {
                        popUpTo(Screen.AddEvent.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onLogOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
