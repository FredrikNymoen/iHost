package no.ntnu.prog2007.ihost.ui.screens.friends

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.data.model.domain.getOtherUserId
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel
import no.ntnu.prog2007.ihost.ui.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendScreen(
    friendViewModel: FriendViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val friendUiState by friendViewModel.uiState.collectAsState()
    val currentUserId = authViewModel.uiState.value.currentUser?.uid

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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                friendUiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = friendUiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { friendViewModel.loadAllUsers() }) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Search field
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally),
                            placeholder = { Text("Search users...") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Filter users: exclude already connected and apply search
                        val filteredUsers = friendUiState.allUsers.filter { user ->
                            user.uid !in connectedUserIds &&
                                    "${user.firstName} ${user.lastName} ${user.username}".contains(
                                        searchQuery,
                                        ignoreCase = true
                                    )
                        }

                        if (filteredUsers.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No users found" else "No users available",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = filteredUsers,
                                    key = { user -> user.uid }
                                ) { user ->
                                    UserItemWithAddButton(
                                        user = user,
                                        onAddFriend = {
                                            friendViewModel.sendFriendRequest(
                                                toUserId = user.uid,
                                                onSuccess = {
                                                    Toast.makeText(
                                                        context,
                                                        "Friend request sent to ${user.firstName}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                onError = { error ->
                                                    Toast.makeText(
                                                        context,
                                                        "Error: $error",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
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
}

@Composable
fun UserItemWithAddButton(
    user: User,
    onAddFriend: () -> Unit
) {
    var requestSent by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // User avatar
                if (user.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.username.firstOrNull()?.uppercase() ?: "?",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                Column {
                    Text(
                        text = "${user.firstName} ${user.lastName ?: ""}".trim(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "@${user.username}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            // Add friend button
            if (requestSent) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Request sent",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        requestSent = true
                        onAddFriend()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add friend",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
