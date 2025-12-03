package no.ntnu.prog2007.ihost.ui.screens.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.ui.components.UserCardWithTwoActions
import no.ntnu.prog2007.ihost.ui.components.UserCardWithIconAction
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendUiState

@Composable
fun FriendsSection(
    friendViewModel: FriendViewModel,
    authViewModel: AuthViewModel,
    friendUiState: FriendUiState,
    onNavigateToAddFriend: () -> Unit,
    onNavigateToFriendsList: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Friends count and action buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Friends count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onNavigateToFriendsList)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Friends",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "${friendUiState.friends.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Friends",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Add Friend button
                FilledTonalButton(
                    onClick = onNavigateToAddFriend,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Friend",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Pending friend requests (received)
        if (friendUiState.pendingRequests.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEFEBE9) // Light brown
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Friend Requests",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4E342E) // Dark brown
                        )
                        Surface(
                            color = Color(0xFF8D6E63), // Medium brown
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${friendUiState.pendingRequests.size}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    friendUiState.pendingRequests.forEach { friendship ->
                        val requesterUser = friendUiState.userDetailsMap[friendship.user1Id]
                        if (requesterUser != null) {
                            UserCardWithTwoActions(
                                user = requesterUser,
                                firstIcon = Icons.Default.Check,
                                firstIconTint = MaterialTheme.colorScheme.primary,
                                firstIconDescription = "Accept",
                                onFirstIconClick = {
                                    friendViewModel.acceptFriendRequest(
                                        friendshipId = friendship.id,
                                        onSuccess = {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Accepted friend request from ${requesterUser.firstName}",
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
                                },
                                secondIcon = Icons.Default.Close,
                                secondIconTint = MaterialTheme.colorScheme.error,
                                secondIconDescription = "Decline",
                                onSecondIconClick = {
                                    friendViewModel.declineFriendRequest(
                                        friendshipId = friendship.id,
                                        onSuccess = {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Declined friend request",
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
                                },
                                backgroundColor = Color(0xFFEFEBE9), // Light brown
                                textColor = Color(0xFF4E342E), // Dark brown
                                showCard = false
                            )
                            if (friendship != friendUiState.pendingRequests.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = Color(0xFF4E342E).copy(alpha = 0.2f) // Dark brown with transparency
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sent friend requests (pending)
        if (friendUiState.sentRequests.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFD7CCC8) // Lighter brown
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sent Requests",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3E2723) // Darker brown
                        )
                        Surface(
                            color = Color(0xFF6D4C41), // Dark brown
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${friendUiState.sentRequests.size}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    friendUiState.sentRequests.forEach { friendship ->
                        val recipientUser = friendUiState.userDetailsMap[friendship.user2Id]
                        if (recipientUser != null) {
                            UserCardWithIconAction(
                                user = recipientUser,
                                icon = Icons.Default.Close,
                                iconTint = MaterialTheme.colorScheme.error,
                                iconDescription = "Cancel request",
                                onIconClick = {
                                    friendViewModel.removeFriend(
                                        friendshipId = friendship.id,
                                        onSuccess = {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Cancelled friend request",
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
                                },
                                backgroundColor = Color(0xFFD7CCC8), // Lighter brown
                                textColor = Color(0xFF3E2723), // Darker brown
                                showCard = false
                            )
                            if (friendship != friendUiState.sentRequests.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = Color(0xFF3E2723).copy(alpha = 0.2f) // Darker brown with transparency
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
