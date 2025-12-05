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