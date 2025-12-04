package no.ntnu.prog2007.ihost.ui.screens.events.inviteusers.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.ui.components.UserCard

@Composable
fun UserSelectionList(
    users: List<User>,
    searchQuery: String,
    selectedUserIds: Set<String>,
    onUserToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            users.filter { user ->
                "${user.firstName} ${user.lastName} ${user.username}".contains(
                    searchQuery, true
                )
            },
            key = { it.uid }
        ) { user ->
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
                    onUserToggle(user.uid)
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
