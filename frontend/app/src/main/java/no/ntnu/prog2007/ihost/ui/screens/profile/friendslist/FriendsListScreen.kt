package no.ntnu.prog2007.ihost.ui.screens.profile.friendslist

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import no.ntnu.prog2007.ihost.ui.components.layout.TopBar
import no.ntnu.prog2007.ihost.ui.screens.profile.friendslist.components.*
import no.ntnu.prog2007.ihost.ui.components.states.ErrorStateWithRetry
import no.ntnu.prog2007.ihost.ui.components.states.LoadingState
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel

/**
 * Friends list screen
 *
 * Displays current user's confirmed friends with quick actions to message or unfriend.
 * Shows total friend count in header. Handles loading, error, and empty states.
 *
 * Features:
 * - List of all confirmed friends
 * - Friend count displayed in title
 * - Message and unfriend actions for each friend
 * - Loading and error state handling
 * - Empty state when user has no friends
 *
 * @param friendViewModel FriendViewModel for friend operations and data
 * @param authViewModel AuthViewModel for current user context
 * @param onBack Callback invoked when user navigates back to Profile
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(
    friendViewModel: FriendViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val friendUiState by friendViewModel.uiState.collectAsState()
    val currentUserId = authViewModel.uiState.collectAsState().value.currentUser?.uid

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBar(
                title = {
                    Text("Friends (${friendUiState.friends.size})")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                friendUiState.isLoading -> {
                    LoadingState()
                }

                friendUiState.errorMessage != null -> {
                    ErrorStateWithRetry(
                        errorMessage = friendUiState.errorMessage!!,
                        onRetry = { friendViewModel.loadFriendships() }
                    )
                }

                friendUiState.friends.isEmpty() -> {
                    EmptyState(message = "No friends yet")
                }

                else -> {
                    FriendsListContent(
                        friends = friendUiState.friends,
                        userDetailsMap = friendUiState.userDetailsMap,
                        currentUserId = currentUserId,
                        friendViewModel = friendViewModel
                    )
                }
            }
        }
    }
}