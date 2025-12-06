package no.ntnu.prog2007.ihost.ui.screens.events.inviteusers.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Floating invite button component
 *
 * Floating action button for sending invitations to selected users.
 * Only visible when at least one user is selected.
 * Shows loading indicator while invitations are being sent.
 *
 * @param selectedCount Number of users currently selected for invitation
 * @param isSending Whether invitation send request is in progress
 * @param onInvite Callback invoked when user clicks the button
 */
@Composable
fun FloatingInviteButton(
    selectedCount: Int,
    isSending: Boolean,
    onInvite: () -> Unit
) {
    if (selectedCount > 0) {
        FloatingActionButton(
            onClick = onInvite,
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
                    Text("Invite ($selectedCount)")
                }
            }
        }
    }
}
