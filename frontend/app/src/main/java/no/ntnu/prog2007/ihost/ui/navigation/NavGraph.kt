package no.ntnu.prog2007.ihost.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import no.ntnu.prog2007.ihost.ui.screens.addevent.AddEventScreen
import no.ntnu.prog2007.ihost.ui.screens.auth.LoginScreen
import no.ntnu.prog2007.ihost.ui.screens.auth.PersonalInfoScreen
import no.ntnu.prog2007.ihost.ui.screens.auth.SignUpScreen
import no.ntnu.prog2007.ihost.ui.screens.events.EditEventScreen
import no.ntnu.prog2007.ihost.ui.screens.events.EventDetailScreen
import no.ntnu.prog2007.ihost.ui.screens.events.EventsScreen
import no.ntnu.prog2007.ihost.ui.screens.events.InviteUsersScreen
import no.ntnu.prog2007.ihost.ui.screens.profile.friends.AddFriendScreen
import no.ntnu.prog2007.ihost.ui.screens.profile.friends.FriendsListScreen
import no.ntnu.prog2007.ihost.ui.screens.profile.ProfileScreen
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel
import no.ntnu.prog2007.ihost.viewmodel.StripeViewModel

/**
 * Extension functions for adding navigation destinations to NavGraphBuilder
 */

// Auth screens
fun NavGraphBuilder.loginScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    composable(Destination.Login.route) {
        LoginScreen(
            viewModel = authViewModel,
            onNavigateToSignUp = {
                authViewModel.resetRegistrationState()
                navController.navigate(Destination.PersonalInfo.route)
            }
        )
    }
}

fun NavGraphBuilder.personalInfoScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    composable(Destination.PersonalInfo.route) {
        PersonalInfoScreen(
            viewModel = authViewModel,
            onNext = {
                navController.navigate(Destination.SignUp.route)
            },
            onNavigateToLogin = {
                navController.popBackStack()
            }
        )
    }
}

fun NavGraphBuilder.signUpScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    composable(Destination.SignUp.route) {
        SignUpScreen(
            viewModel = authViewModel,
            onNavigateToLogin = {
                navController.popBackStack()
            },
            onNavigateBack = {
                navController.navigate(Destination.PersonalInfo.route)
            }
        )
    }
}

// Main app screens
fun NavGraphBuilder.eventsScreen(
    navController: NavHostController,
    eventViewModel: EventViewModel,
    authViewModel: AuthViewModel
) {
    composable(Destination.Events.route) {
        EventsScreen(
            viewModel = eventViewModel,
            authViewModel = authViewModel,
            onEventClick = { eventId ->
                navController.navigate(Destination.EventDetail.createRoute(eventId))
            }
        )
    }
}

fun NavGraphBuilder.addEventScreen(
    navController: NavHostController,
    eventViewModel: EventViewModel
) {
    composable(Destination.AddEvent.route) {
        AddEventScreen(
            viewModel = eventViewModel,
            onEventCreated = {
                navController.navigate(Destination.Events.route) {
                    popUpTo(Destination.AddEvent.route) { inclusive = true }
                }
            }
        )
    }
}

fun NavGraphBuilder.profileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    friendViewModel: FriendViewModel
) {
    composable(Destination.Profile.route) {
        ProfileScreen(
            authViewModel = authViewModel,
            eventViewModel = eventViewModel,
            friendViewModel = friendViewModel,
            onLogOut = {
                // Clear all ViewModels state on logout
                eventViewModel.clearEvents()
                friendViewModel.clearFriendships()

                navController.navigate(Destination.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateToAddFriend = {
                navController.navigate(Destination.AddFriend.route)
            },
            onNavigateToFriendsList = {
                navController.navigate(Destination.FriendsList.route)
            }
        )
    }
}

// Detail screens with arguments
fun NavGraphBuilder.eventDetailScreen(
    navController: NavHostController,
    eventViewModel: EventViewModel,
    authViewModel: AuthViewModel,
    stripeViewModel: StripeViewModel
) {
    composable(
        route = Destination.EventDetail.route,
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
            onEdit = {
                navController.navigate(Destination.EditEvent.createRoute(eventId))
            },
            onInviteUsers = { id ->
                navController.navigate(Destination.InviteUsers.createRoute(id))
            }
        )
    }
}

fun NavGraphBuilder.editEventScreen(
    navController: NavHostController,
    eventViewModel: EventViewModel
) {
    composable(
        route = Destination.EditEvent.route,
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
}

fun NavGraphBuilder.inviteUsersScreen(
    navController: NavHostController,
    eventViewModel: EventViewModel,
    friendViewModel: FriendViewModel,
    authViewModel: AuthViewModel
) {
    composable(
        route = Destination.InviteUsers.route,
        arguments = listOf(
            navArgument("eventId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
        InviteUsersScreen(
            eventId = eventId,
            viewModel = eventViewModel,
            friendViewModel = friendViewModel,
            authViewModel = authViewModel,
            onBack = {
                navController.popBackStack()
            }
        )
    }
}

// Friend screens
fun NavGraphBuilder.addFriendScreen(
    navController: NavHostController,
    friendViewModel: FriendViewModel,
    authViewModel: AuthViewModel
) {
    composable(Destination.AddFriend.route) {
        AddFriendScreen(
            friendViewModel = friendViewModel,
            authViewModel = authViewModel,
            onBack = {
                navController.popBackStack()
            }
        )
    }
}

fun NavGraphBuilder.friendsListScreen(
    navController: NavHostController,
    friendViewModel: FriendViewModel,
    authViewModel: AuthViewModel
) {
    composable(Destination.FriendsList.route) {
        FriendsListScreen(
            friendViewModel = friendViewModel,
            authViewModel = authViewModel,
            onBack = {
                navController.popBackStack()
            }
        )
    }
}
