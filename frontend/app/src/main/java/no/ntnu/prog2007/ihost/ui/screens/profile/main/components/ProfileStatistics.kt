package no.ntnu.prog2007.ihost.ui.screens.profile.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Profile statistics component
 *
 * Displays event-related statistics for the user's profile.
 * Shows count of created events and invitations received.
 *
 * @param eventsCreated Number of events user has created
 * @param eventsInvitedTo Number of events user has been invited to
 * @param modifier Optional Modifier for customizing layout
 */
@Composable
fun ProfileStatistics(
    eventsCreated: Int,
    eventsInvitedTo: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$eventsCreated events created",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = " • ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = "$eventsInvitedTo invitations",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
