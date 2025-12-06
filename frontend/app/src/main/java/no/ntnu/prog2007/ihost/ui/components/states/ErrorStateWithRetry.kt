package no.ntnu.prog2007.ihost.ui.components.states

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Full-screen error state with retry button
 *
 * Displays an error message with a retry button, centered on screen.
 * Used when data loading fails and user can retry the operation.
 *
 * @param errorMessage Error message to display to user
 * @param onRetry Callback invoked when retry button is clicked
 * @param modifier Optional modifier for styling
 */
@Composable
fun ErrorStateWithRetry(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
