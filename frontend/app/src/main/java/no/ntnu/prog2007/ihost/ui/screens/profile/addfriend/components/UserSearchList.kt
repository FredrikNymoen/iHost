package no.ntnu.prog2007.ihost.ui.screens.profile.addfriend.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel

@Composable
fun UserSearchList(
    filteredUsers: List<User>,
    searchQuery: String,
    friendViewModel: FriendViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (filteredUsers.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
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
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredUsers,
                key = { user -> user.uid.ifEmpty { user.hashCode().toString() } }

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
