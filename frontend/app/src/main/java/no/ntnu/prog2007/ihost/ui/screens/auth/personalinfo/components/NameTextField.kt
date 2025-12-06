package no.ntnu.prog2007.ihost.ui.screens.auth.personalinfo.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Name input field component
 *
 * Text field for entering first or last name during account creation.
 * Displays person icon and supports both first name and last name inputs via dynamic label.
 *
 * @param value Current name input value
 * @param onValueChange Callback invoked when name value changes
 * @param label Display label for the field (e.g., "First Name" or "Last Name")
 * @param modifier Optional Modifier for customizing layout
 */
@Composable
fun NameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onSurface) },
        leadingIcon = {
            Icon(
                Icons.Default.Person,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}
