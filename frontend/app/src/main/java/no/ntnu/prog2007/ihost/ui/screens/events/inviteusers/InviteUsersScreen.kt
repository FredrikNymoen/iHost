package no.ntnu.prog2007.ihost.ui.screens.events.inviteusers

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.data.model.domain.getOtherUserId
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.ui.components.TopBar
import no.ntnu.prog2007.ihost.ui.screens.events.inviteusers.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteUsersScreen(
    eventId: String,
    viewModel: EventViewModel,
    friendViewModel: FriendViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val friendUiState by friendViewModel.uiState.collectAsState()
    val currentUserId = authViewModel.uiState.collectAsState().value.currentUser?.uid

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var currentEventAttendees by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }  // This is what user types


    LaunchedEffect(searchText) {
        delay(500)  // Wait 500ms
        searchQuery = searchText  // Then update the actual search query
    }

    // Ensure friends are loaded
    LaunchedEffect(Unit) {
        if (friendUiState.friends.isEmpty() && !friendUiState.isLoading) {
            friendViewModel.loadFriendships()
        }
    }

    // Load friends and current attendees
    LaunchedEffect(eventId, friendUiState.friends, friendUiState.userDetailsMap, currentUserId) {
        isLoading = true
        try {
            // Get friends from friendViewModel
            val friendUsers = if (currentUserId != null) {
                friendUiState.friends.mapNotNull { friendship ->
                    val friendUserId = friendship.getOtherUserId(currentUserId)
                    friendUiState.userDetailsMap[friendUserId]
                }
            } else {
                emptyList()
            }
            users = friendUsers

            // Get current event attendees from UI state
            val eventAttendees = uiState.eventAttendees[eventId] ?: emptyList()
            currentEventAttendees = eventAttendees.map { it.userId }
        } catch (e: Exception) {
            errorMessage = "Failed to load friends: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Filter out users already invited
    val availableUsers = users.filter { it.uid !in currentEventAttendees }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(
                title = { Text("Invite Users") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingInviteButton(
                selectedCount = selectedUserIds.size,
                isSending = isSending,
                onInvite = {
                    isSending = true
                    viewModel.inviteUsers(
                        eventId = eventId,
                        userIds = selectedUserIds.toList(),
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "${selectedUserIds.size} user(s) invited!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onBack()
                        },
                        onError = { error ->
                            Toast.makeText(
                                context,
                                "Error: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                            isSending = false
                        }
                    )
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
                isLoading -> {
                    LoadingState()
                }

                errorMessage != null -> {
                    ErrorState(
                        errorMessage = errorMessage,
                        onGoBack = onBack
                    )
                }

                availableUsers.isEmpty() -> {
                    EmptyState(hasUsers = users.isNotEmpty())
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        SearchBar(
                            searchText = searchText,
                            onSearchTextChange = { searchText = it },
                            isLoading = uiState.isLoading,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        // User list
                        UserSelectionList(
                            users = availableUsers,
                            searchQuery = searchQuery,
                            selectedUserIds = selectedUserIds,
                            onUserToggle = { userId ->
                                selectedUserIds = if (userId in selectedUserIds) {
                                    selectedUserIds - userId
                                } else {
                                    selectedUserIds + userId
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
