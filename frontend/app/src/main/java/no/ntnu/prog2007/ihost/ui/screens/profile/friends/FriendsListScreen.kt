package no.ntnu.prog2007.ihost.ui.screens.profile.friends

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.data.model.domain.getOtherUserId
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel
import no.ntnu.prog2007.ihost.ui.components.UserCardWithIconAction
import no.ntnu.prog2007.ihost.ui.components.TopBar

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
                                UserCardWithIconAction(
                                    user = friendUser,
                                    icon = Icons.Default.PersonRemove,
                                    iconTint = MaterialTheme.colorScheme.error,
                                    iconDescription = "Remove friend",
                                    onIconClick = {
                                        friendViewModel.removeFriend(
                                            friendshipId = friendship.id,
                                            onSuccess = {
                                                Toast.makeText(
                                                    context,
                                                    "Removed ${friendUser.firstName} from friends",
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