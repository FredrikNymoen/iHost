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
            color = Color.Blue // Background color of the dialog
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
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input field for the code
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = {
                        codeInput = it
                        isError = false // Reset error state on input change
                    },
                    label = { Text("Enter Share Code", color = Color.White) },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFC107),
                        unfocusedBorderColor = Color(0xFFFFC107),
                        cursorColor = Color(0xFFFFC107)
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
                        Text("Cancel", color = Color.White)
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
                        colors = ButtonDefaults.buttonColors( // Colors copied from AddEventScreen.kt
                            containerColor = Color(0xFFFFC107),
                            contentColor = Color(0xFF001D3D),
                            disabledContainerColor = Color(0xFFB8860B),
                            disabledContentColor = Color(0xFF001D3D)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = Color(0xFF001D3D),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                "Join Event",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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