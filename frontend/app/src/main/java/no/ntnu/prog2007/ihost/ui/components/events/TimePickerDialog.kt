package no.ntnu.prog2007.ihost.ui.components.events

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

/**
 * Time picker dialog wrapper component
 *
 * A wrapper around Material3 AlertDialog specifically configured for time picking.
 * Provides a consistent dialog container for time selection UI.
 *
 * @param onDismissRequest Callback invoked when dialog should be dismissed
 * @param confirmButton Composable for the confirm action button
 * @param dismissButton Composable for the dismiss/cancel action button
 * @param content Composable content to display (typically a TimePicker)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = {
            content()
        }
    )
}
