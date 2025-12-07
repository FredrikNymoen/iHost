package no.ntnu.prog2007.ihost.ui.screens.auth.welcome.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Welcome screen action buttons
 *
 * Displays primary "Login" button and secondary "Register" outlined button
 * stacked vertically for authentication navigation.
 *
 * @param onNavigateToLogin Callback invoked when Login button is clicked
 * @param onNavigateToSignUp Callback invoked when Register button is clicked
 * @param modifier Optional modifier for styling and layout customization
 */
@Composable
fun WelcomeButtons(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Login", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateToSignUp,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Register", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
