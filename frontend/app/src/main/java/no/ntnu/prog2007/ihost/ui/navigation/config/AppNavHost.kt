package no.ntnu.prog2007.ihost.ui.navigation.config

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import no.ntnu.prog2007.ihost.ui.navigation.graph.*
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel
import no.ntnu.prog2007.ihost.viewmodel.UserViewModel

/**
 * Main navigation host for the app
 *
 * Orchestrates the entire navigation graph by composing all screen destinations
 * including authentication screens, main app screens, and detail screens.
 * Acts as the central routing configuration for the application.
 *
 * @param navController Navigation controller for managing screen transitions
 * @param authViewModel ViewModel for authentication operations
 * @param eventViewModel ViewModel for event operations
 * @param friendViewModel ViewModel for friendship operations
 * @param userViewModel ViewModel for user profile operations
 * @param modifier Optional modifier for styling
 * @param startDestination Initial destination route (Events for logged in, Welcome otherwise)
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    friendViewModel: FriendViewModel,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier,
    startDestination: String = Destination.Welcome.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth screens
        welcomeScreen(navController, authViewModel)
        loginScreen(navController, authViewModel)
        forgotPasswordScreen(navController, authViewModel)
        personalInfoScreen(navController, authViewModel)
        signUpScreen(navController, authViewModel)

        // Main app screens
        eventsScreen(navController, eventViewModel, authViewModel)
        addEventScreen(navController, eventViewModel)
        profileScreen(navController, authViewModel, userViewModel, eventViewModel, friendViewModel)

        // Detail screens with arguments
        eventDetailScreen(navController, eventViewModel, authViewModel)
        editEventScreen(navController, eventViewModel)
        inviteUsersScreen(navController, eventViewModel, friendViewModel, authViewModel)

        // Friend screens
        addFriendScreen(navController, friendViewModel, authViewModel)
        friendsListScreen(navController, friendViewModel, authViewModel)
    }
}
