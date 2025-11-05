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
import no.ntnu.prog2007.ihost.data.model.User
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteUsersScreen(
    eventId: String,
    viewModel: EventViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var currentEventAttendees by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load users and current attendees
    LaunchedEffect(eventId) {
        isLoading = true
        try {
            // Fetch all users
            users = viewModel.getAllUsers()

            // Get current event attendees from UI state
            val eventAttendees = uiState.eventAttendees[eventId] ?: emptyList()
            currentEventAttendees = eventAttendees.map { it.userId }
        } catch (e: Exception) {
            errorMessage = "Failed to load users: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Filter out users already invited
    val availableUsers = users.filter { it.uid !in currentEventAttendees }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Invite Users", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF001D3D)
                )
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
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color(0xFF001D3D)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF001D3D),
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
                        color = Color(0xFFFFC107)
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
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107),
                                contentColor = Color(0xFF001D3D)
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
                            text = "All users have been invited",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Selected count
                        if (selectedUserIds.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF001D3D)
                            ) {
                                Text(
                                    text = "${selectedUserIds.size} user(s) selected",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFFFFC107),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // User list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableUsers) { user ->
                                UserItem(
                                    user = user,
                                    isSelected = user.uid in selectedUserIds,
                                    onToggle = {
                                        selectedUserIds = if (user.uid in selectedUserIds) {
                                            selectedUserIds - user.uid
                                        } else {
                                            selectedUserIds + user.uid
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

@Composable
fun UserItem(
    user: User,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        color = if (isSelected) Color(0xFF0C5CA7) else Color(0xFF001D3D),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp
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
                // User avatar placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFFFFC107),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.displayName.firstOrNull()?.uppercase() ?: "?",
                        color = Color(0xFF001D3D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Column {
                    Text(
                        text = user.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = user.email,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            // Checkbox
            if (isSelected) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    color = Color(0xFFFFC107),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color(0xFF001D3D),
                        modifier = Modifier.padding(6.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
