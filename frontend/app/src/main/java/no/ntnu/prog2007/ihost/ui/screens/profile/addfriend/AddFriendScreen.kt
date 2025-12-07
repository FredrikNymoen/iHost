package no.ntnu.prog2007.ihost.ui.screens.profile.addfriend

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import no.ntnu.prog2007.ihost.data.model.domain.getOtherUserId
import no.ntnu.prog2007.ihost.ui.components.layout.TopBar
import no.ntnu.prog2007.ihost.ui.components.states.ErrorStateWithRetry
import no.ntnu.prog2007.ihost.ui.components.states.LoadingState
import no.ntnu.prog2007.ihost.ui.screens.profile.addfriend.components.*
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel
import androidx.compose.runtime.collectAsState

/**
 * Add friend screen
 *
 * Allows users to search for and send friend requests to other users.
 * Displays all users excluding those already connected (friends, pending, or sent requests).
 * Includes search functionality with 300ms debounce for performance.
 *
 * Features:
 * - Search bar to find users by name or username
 * - User list excluding connected users
 * - Quick "Add Friend" buttons for each user
 * - Loading and error state handling
 * - Real-time friend request status updates
 *
 * @param friendViewModel FriendViewModel for friend operations
 * @param authViewModel AuthViewModel for current user context
 * @param onBack Callback invoked when user navigates back to Profile
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendScreen(
    friendViewModel: FriendViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val friendUiState by friendViewModel.uiState.collectAsState()
    val currentUserId = authViewModel.uiState.collectAsState().value.currentUser?.uid

    var searchQuery by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }

    // Debounce search
    LaunchedEffect(searchText) {
        delay(300)
        searchQuery = searchText
    }

    // Load all users when screen opens
    LaunchedEffect(Unit) {
        friendViewModel.loadAllUsers()
        friendViewModel.loadFriendships()
    }

    // Get list of user IDs that current user has relationship with
    val connectedUserIds = remember(friendUiState.friends, friendUiState.pendingRequests, friendUiState.sentRequests) {
        val friendIds = friendUiState.friends.mapNotNull {
            if (currentUserId != null) it.getOtherUserId(currentUserId) else null
        }
        val pendingIds = friendUiState.pendingRequests.map { it.user1Id }
        val sentIds = friendUiState.sentRequests.map { it.user2Id }
        (friendIds + pendingIds + sentIds).toSet()
    }

    // Filter users: exclude already connected and apply search
    val filteredUsers = friendUiState.allUsers.filter { user ->
        user.uid !in connectedUserIds &&
                "${user.firstName} ${user.lastName} ${user.username}".contains(
                    searchQuery,
                    ignoreCase = true
                )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(
                title = { Text("Add Friend") },
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
                friendUiState.isLoading && friendUiState.allUsers.isEmpty() -> {
                    LoadingState()
                }

                friendUiState.errorMessage != null -> {
                    ErrorStateWithRetry(
                        errorMessage = friendUiState.errorMessage!!,
                        onRetry = { friendViewModel.loadAllUsers() }
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Search field
                        SearchField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        // User list with search results
                        UserSearchList(
                            filteredUsers = filteredUsers,
                            searchQuery = searchQuery,
                            friendViewModel = friendViewModel
                        )
                    }
                }
            }
        }
    }
}
