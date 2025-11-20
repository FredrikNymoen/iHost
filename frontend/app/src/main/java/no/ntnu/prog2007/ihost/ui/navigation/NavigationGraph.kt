package no.ntnu.prog2007.ihost.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import no.ntnu.prog2007.ihost.ui.screens.auth.LoginScreen
import no.ntnu.prog2007.ihost.ui.screens.auth.SignUpScreen
import no.ntnu.prog2007.ihost.ui.screens.events.EventsScreen
import no.ntnu.prog2007.ihost.ui.screens.events.EventDetailScreen
import no.ntnu.prog2007.ihost.ui.screens.events.InviteUsersScreen
import no.ntnu.prog2007.ihost.ui.screens.addevent.AddEventScreen
import no.ntnu.prog2007.ihost.ui.screens.auth.PersonalInfoScreen
import no.ntnu.prog2007.ihost.ui.screens.editevent.EditEventScreen
import no.ntnu.prog2007.ihost.ui.screens.profile.ProfileScreen
import no.ntnu.prog2007.ihost.ui.screens.friends.AddFriendScreen
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.StripeViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel

@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    stripeViewModel: StripeViewModel,
    friendViewModel: FriendViewModel,
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
                    authViewModel.resetRegistrationState()
                    navController.navigate(Screen.PersonalInfo.route)
                }
            )
        }

        composable(Screen.PersonalInfo.route) {
            PersonalInfoScreen(
                viewModel = authViewModel,
                onNext = {
                    navController.navigate(Screen.SignUp.route)
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.navigate(Screen.PersonalInfo.route)
                }
            )
        }

        // Main app screens
        composable(Screen.Events.route) {
            EventsScreen(
                viewModel = eventViewModel,
                authViewModel = authViewModel,
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                }
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDetailScreen(
                eventId = eventId,
                viewModel = eventViewModel,
                authViewModel = authViewModel,
                stripeViewModel = stripeViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onEdit = { id ->
                    navController.navigate(Screen.EditEvent.createRoute(eventId))
                },
                onInviteUsers = { id ->
                    navController.navigate(Screen.InviteUsers.createRoute(id))
                }
            )
        }
        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EditEventScreen(
                eventId = eventId,
                viewModel = eventViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onEventUpdated = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.InviteUsers.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            InviteUsersScreen(
                eventId = eventId,
                viewModel = eventViewModel,
                onBack = {
                    navController.popBackStack()
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
                eventViewModel = eventViewModel,
                friendViewModel = friendViewModel,
                onLogOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToAddFriend = {
                    navController.navigate(Screen.AddFriend.route)
                }
            )
        }

        composable(Screen.AddFriend.route) {
            AddFriendScreen(
                friendViewModel = friendViewModel,
                authViewModel = authViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        //composable(Screen.Login)
    }
}
