package no.ntnu.prog2007.ihost.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel
import no.ntnu.prog2007.ihost.viewmodel.StripeViewModel

/**
 * Main navigation host for the app
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    stripeViewModel: StripeViewModel,
    friendViewModel: FriendViewModel,
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
        profileScreen(navController, authViewModel, eventViewModel, friendViewModel)

        // Detail screens with arguments
        eventDetailScreen(navController, eventViewModel, authViewModel, stripeViewModel)
        editEventScreen(navController, eventViewModel)
        inviteUsersScreen(navController, eventViewModel, friendViewModel, authViewModel)

        // Friend screens
        addFriendScreen(navController, friendViewModel, authViewModel)
        friendsListScreen(navController, friendViewModel, authViewModel)
    }
}
