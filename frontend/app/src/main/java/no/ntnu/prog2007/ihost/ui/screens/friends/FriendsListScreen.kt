package no.ntnu.prog2007.ihost.ui.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import no.ntnu.prog2007.ihost.data.model.User
import no.ntnu.prog2007.ihost.data.model.getOtherUserId
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(
    friendViewModel: FriendViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val friendUiState by friendViewModel.uiState.collectAsState()
    val currentUserId = authViewModel.uiState.collectAsState().value.currentUser?.uid

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Friends (${friendUiState.friends.size})",
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier.height(64.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                friendUiState.isLoading -> {
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
                        Button(onClick = { friendViewModel.loadFriendships() }) {
                            Text("Retry")
                        }
                    }
                }

                friendUiState.friends.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No friends yet",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = friendUiState.friends,
                            key = { it.id }
                        ) { friendship ->
                            val friendUserId = if (currentUserId != null)
                                friendship.getOtherUserId(currentUserId)
                            else null
                            val friendUser = friendUserId?.let { friendUiState.userDetailsMap[it] }

                            if (friendUser != null) {
                                FriendListItem(
                                    user = friendUser,
                                    onRemove = {
                                        friendViewModel.removeFriend(
                                            friendshipId = friendship.id,
                                            onSuccess = {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Removed ${friendUser.firstName} from friends",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onError = { error ->
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Error: $error",
                                                    android.widget.Toast.LENGTH_SHORT
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

@Composable
fun FriendListItem(
    user: User,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // User avatar
                if (user.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
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

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "${user.firstName} ${user.lastName ?: ""}".trim(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@${user.username}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonRemove,
                    contentDescription = "Remove friend",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
