package no.ntnu.prog2007.ihost.ui.screens.auth.login.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

/**
 * Clickable "Forgot Password?" link
 *
 * Displays a text button link that navigates to password reset flow
 * when clicked. Aligned to the start of the container.
 *
 * @param onClick Callback invoked when link is clicked
 * @param modifier Optional modifier for styling and layout customization
 */
@Composable
fun ForgotPasswordLink(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        TextButton(onClick = onClick) {
            Text("Forgot Password?", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}
