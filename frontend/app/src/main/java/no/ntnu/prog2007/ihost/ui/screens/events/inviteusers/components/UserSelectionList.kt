package no.ntnu.prog2007.ihost.ui.screens.events.inviteusers.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.data.model.domain.User

/**
 * Searchable list of users for event invitation
 *
 * Displays a scrollable list of users that can be selected for event invitations.
 * Automatically filters users based on search query matching name or username.
 * Shows selection state for each user.
 *
 * @param users Complete list of users available to invite
 * @param searchQuery Current search query text for filtering users
 * @param selectedUserIds Set of UIDs for currently selected users
 * @param onUserToggle Callback invoked when user selection is toggled
 * @param modifier Optional modifier for styling and layout customization
 */
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
            UserSelectionCard(
                user = user,
                isSelected = user.uid in selectedUserIds,
                onToggle = { onUserToggle(user.uid) }
            )
        }
    }
}
