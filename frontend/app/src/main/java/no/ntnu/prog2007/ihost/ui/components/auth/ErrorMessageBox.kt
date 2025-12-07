package no.ntnu.prog2007.ihost.ui.components.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Error message display box
 *
 * Shows an error message in a styled container with error colors.
 * Automatically hides when errorMessage is null.
 *
 * @param errorMessage Error message to display, or null to hide
 * @param modifier Optional modifier for styling
 */
@Composable
fun ErrorMessageBox(
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    if (errorMessage != null) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp
            )
        }
    }
}
