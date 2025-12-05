package no.ntnu.prog2007.ihost.ui.navigation.state

import androidx.compose.runtime.*
import no.ntnu.prog2007.ihost.viewmodel.AuthUiState
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel
import no.ntnu.prog2007.ihost.viewmodel.UserViewModel

/**
 * Handles navigation side effects based on authentication state
 */
@Composable
fun NavigationEffects(
    authUiState: AuthUiState,
    navigationState: NavigationState,
    eventViewModel: EventViewModel,
    friendViewModel: FriendViewModel,
    userViewModel: UserViewModel,
) {
    var hasNavigatedOnLogin by remember { mutableStateOf(false) }

    // Navigate when auth state changes
    LaunchedEffect(authUiState.isLoggedIn, authUiState.isLoading, navigationState.currentRoute) {
        if (authUiState.isLoggedIn && !authUiState.isLoading) {
            // User just logged in/signed up and loading is done
            if (navigationState.isOnAuthScreen) {
                if (!hasNavigatedOnLogin) {
                    hasNavigatedOnLogin = true
                    // Load fresh events for the new user
                    eventViewModel.loadEvents()
                    // Navigate to Events
                    navigationState.navigateToEvents()
                }
            }
        } else if (!authUiState.isLoggedIn) {
            // Reset flag when user is logged out
            hasNavigatedOnLogin = false
            // If user logged out and not on auth screens, navigate back to Welcome
            if (!navigationState.isOnAuthScreen && navigationState.currentRoute != null) {
                // Clear all ViewModels
                clearAllViewModels(
                    eventViewModel = eventViewModel,
                    friendViewModel = friendViewModel,
                    userViewModel = userViewModel
                )
                navigationState.navigateToWelcome()
            }
        }
    }
}

/**
 * Clears all ViewModels on logout
 */
private fun clearAllViewModels(
    eventViewModel: EventViewModel,
    friendViewModel: FriendViewModel,
    userViewModel: UserViewModel
) {
    eventViewModel.clearEvents()
    friendViewModel.clearFriendships()
    userViewModel.clearUserData()
}
