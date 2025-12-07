package no.ntnu.prog2007.ihost.ui.screens.events.inviteusers.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Error state display for invite users screen
 *
 * Shows an error message with a "Go Back" button when an error occurs
 * during user invitation process.
 *
 * @param errorMessage Error message to display, null shows "Unknown error"
 * @param onGoBack Callback invoked when "Go Back" button is clicked
 */
@Composable
fun ErrorState(
    errorMessage: String?,
    onGoBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = errorMessage ?: "Unknown error",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onGoBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Go Back")
        }
    }
}

/**
 * Empty state display for invite users screen
 *
 * Shows a centered message when there are no users to invite.
 * Message varies based on whether user has friends or not.
 *
 * @param hasUsers Whether the user has any friends in their list
 */
@Composable
fun EmptyState(
    hasUsers: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (hasUsers) "All friends have been invited" else "You have no friends to invite",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Loading state display for invite users screen
 *
 * Shows a centered circular progress indicator while loading user data.
 */
@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}
