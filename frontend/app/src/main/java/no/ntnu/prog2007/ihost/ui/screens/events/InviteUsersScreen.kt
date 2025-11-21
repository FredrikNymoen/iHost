package no.ntnu.prog2007.ihost.ui.screens.events

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import no.ntnu.prog2007.ihost.data.model.User
import no.ntnu.prog2007.ihost.data.model.getOtherUserId
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.ui.components.UserCard
import no.ntnu.prog2007.ihost.ui.components.TopBar

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
            if (selectedUserIds.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
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
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Send Invites"
                            )
                            Text("Invite (${selectedUserIds.size})")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Go Back")
                        }
                    }
                }

                availableUsers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (users.isEmpty()) "You have no friends to invite" else "All friends have been invited",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(top = 32.dp)
                                .padding(bottom = 16.dp)
                                .align(Alignment.CenterHorizontally),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isLoading,
                            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // User list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableUsers.filter { it ->

                                "${it.firstName} ${it.lastName} ${it.username}".contains(
                                    searchQuery, true
                                )

                            }, key = { it.uid }) { user ->
                                UserCard(
                                    user = user,
                                    backgroundColor = if (user.uid in selectedUserIds)
                                        MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.surface,
                                    textColor = if (user.uid in selectedUserIds)
                                        MaterialTheme.colorScheme.onSecondary
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    onClick = {
                                        selectedUserIds = if (user.uid in selectedUserIds) {
                                            selectedUserIds - user.uid
                                        } else {
                                            selectedUserIds + user.uid
                                        }
                                    },
                                    trailingContent = {
                                        if (user.uid in selectedUserIds) {
                                            Surface(
                                                modifier = Modifier.size(32.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.padding(6.dp)
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}