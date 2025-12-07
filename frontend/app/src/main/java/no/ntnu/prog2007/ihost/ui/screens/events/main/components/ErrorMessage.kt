package no.ntnu.prog2007.ihost.ui.screens.events.main.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Error message snackbar component
 *
 * Displays an error message in a snackbar with a "Close" action button.
 * Used for showing temporary error notifications on the events screen.
 *
 * @param message Error message text to display
 * @param onDismiss Callback invoked when snackbar is dismissed or closed
 */
@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    ) {
        Text(message)
    }
}
