package no.ntnu.prog2007.ihost.ui.components.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Button with loading state indicator
 *
 * Displays a circular progress indicator when loading, otherwise shows
 * the button text. Automatically disables interaction during loading.
 *
 * @param onClick Callback invoked when button is clicked
 * @param text Button text to display when not loading
 * @param isLoading Whether to show loading indicator instead of text
 * @param enabled Whether the button is enabled (disabled during loading)
 * @param modifier Optional modifier for styling
 */
@Composable
fun LoadingButton(
    onClick: () -> Unit,
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
