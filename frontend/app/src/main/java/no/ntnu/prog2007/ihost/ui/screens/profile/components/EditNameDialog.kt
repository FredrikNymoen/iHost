package no.ntnu.prog2007.ihost.ui.screens.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EditNameDialog(
    currentFirstName: String,
    currentLastName: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var firstName by remember { mutableStateOf(currentFirstName) }
    var lastName by remember { mutableStateOf(currentLastName) }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Name",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (firstName.isNotBlank()) {
                        onSave(firstName, lastName.ifBlank { null } ?: "")
                    }
                },
                enabled = firstName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
