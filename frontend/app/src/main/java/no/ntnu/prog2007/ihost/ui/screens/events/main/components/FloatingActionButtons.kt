package no.ntnu.prog2007.ihost.ui.screens.events.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Floating action buttons for event screen actions
 *
 * Displays a vertical stack of two floating action buttons:
 * - Primary button for joining events via share code
 * - Secondary button for refreshing event list
 *
 * @param onJoinEvent Callback invoked when join event button is clicked
 * @param onRefresh Callback invoked when refresh button is clicked
 */
@Composable
fun FloatingActionButtons(
    onJoinEvent: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.offset(y = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FloatingActionButton(
            onClick = onJoinEvent,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.LocalActivity,
                contentDescription = "Join Event with Code"
            )
        }

        FloatingActionButton(
            onClick = onRefresh,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh"
            )
        }
    }
}
