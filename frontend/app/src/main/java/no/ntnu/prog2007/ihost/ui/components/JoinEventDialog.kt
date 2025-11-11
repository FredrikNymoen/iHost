package no.ntnu.prog2007.ihost.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog


/**
 * Dialog for joining events using a provided share code.
 */
@Composable
fun JoinEventDialog(
    onDismiss: () -> Unit, // Called when the dialog is dismissed
    onSubmit: (String) -> Unit, // Called with the entered code when submitted
    isLoading: Boolean = false
) {
    var codeInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface( // The main container for the dialog
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface // Background color of the dialog
        ) {
            Column( // Layout the dialog content vertically
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Title
                Text(
                    text = "Join Event with Code",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input field for the code
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = {
                        codeInput = it
                        isError = false // Reset error state on input change
                    },
                    label = { Text("Enter Share Code", color = MaterialTheme.colorScheme.onSurface) },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                if (isError) { // Show error message if code is invalid
                    Text(
                        text = "Invalid code. Please try again.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Cancel button
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Join button
                    Button(
                        onClick = {
                            if (isValidEventCode(codeInput)) {
                                onSubmit(codeInput)
                            } else {
                                isError = true
                            }
                        },
                        enabled = codeInput.isNotBlank() && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                "Join Event",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Validates if the input matches expected event code format
 * i.e., "IH-XXXXX" where X is an uppercase letter or digit.
 */
private fun isValidEventCode(code: String): Boolean {
    val pattern = Regex("IH-[A-Z0-9]{5}")
    return pattern.matches(code)
}