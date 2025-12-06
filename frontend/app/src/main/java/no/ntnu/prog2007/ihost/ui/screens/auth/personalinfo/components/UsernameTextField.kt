package no.ntnu.prog2007.ihost.ui.screens.auth.personalinfo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Username input field component
 *
 * Specialized text field for username entry with real-time validation feedback.
 * Displays check/cross icons and loading spinner based on validation state.
 *
 * Features:
 * - Real-time availability checking with visual indicators
 * - Error message display for validation failures
 * - Loading spinner during async validation
 *
 * @param value Current username input value
 * @param onValueChange Callback invoked when username value changes
 * @param isCheckingUsername Whether username availability is currently being checked
 * @param isUsernameAvailable null=unchecked, true=available, false=taken
 * @param usernameError Error message to display if validation fails; null if valid
 * @param modifier Optional Modifier for customizing layout
 */
@Composable
fun UsernameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isCheckingUsername: Boolean,
    isUsernameAvailable: Boolean?,
    usernameError: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Username", color = MaterialTheme.colorScheme.onSurface) },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Username",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            trailingIcon = {
                when {
                    isCheckingUsername -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    isUsernameAvailable == true -> {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Username available",
                            tint = Color.Green
                        )
                    }
                    isUsernameAvailable == false -> {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Username not available",
                            tint = Color.Red
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        if (usernameError != null) {
            Text(
                text = usernameError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
