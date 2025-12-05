package no.ntnu.prog2007.ihost.ui.navigation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import no.ntnu.prog2007.ihost.viewmodel.AuthUiState
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.UserViewModel

/**
 * Handles initial data loading when app starts with logged in user
 */
@Composable
fun DataLoadingEffects(
    authUiState: AuthUiState,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel
) {
    // Validate authentication and load data on app start if already logged in
    LaunchedEffect(Unit) {
        if (authUiState.isLoggedIn && authUiState.currentUser != null) {
            // Load user profile first to validate authentication with backend
            // This will automatically sign out if token is invalid
            userViewModel.loadUserProfile(authUiState.currentUser.uid)
            // Load events
            eventViewModel.ensureEventsLoaded()
        }
    }
}
