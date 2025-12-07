package no.ntnu.prog2007.ihost.ui.screens.auth.login.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.testTag

/**
 * Email input text field for authentication forms
 *
 * Displays an outlined text field configured for email input with email icon,
 * email keyboard type, and input validation support.
 *
 * @param value Current email text value
 * @param onValueChange Callback invoked when email text changes
 * @param enabled Whether the field is enabled for user input
 * @param modifier Optional modifier for styling and layout customization
 */
@Composable
fun EmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .testTag("emailField"),
        enabled = enabled
    )
}
