package no.ntnu.prog2007.ihost.ui.screens.auth.forgotpassword

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.ui.components.auth.AuthHeader
import no.ntnu.prog2007.ihost.ui.components.auth.ErrorMessageBox
import no.ntnu.prog2007.ihost.ui.screens.auth.forgotpassword.components.EmailResetForm
import no.ntnu.prog2007.ihost.ui.screens.auth.forgotpassword.components.EmailSentConfirmation
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var emailSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!emailSent) {
            AuthHeader(
                title = "Reset Password",
                subtitle = "Enter your email to receive a password reset link"
            )

            Spacer(modifier = Modifier.height(32.dp))

            ErrorMessageBox(errorMessage = uiState.errorMessage)

            EmailResetForm(
                email = email,
                onEmailChange = { email = it },
                isLoading = uiState.isLoading,
                onSendResetLink = {
                    android.util.Log.d("ForgotPasswordScreen", "Send Reset Link clicked for email: $email")
                    viewModel.sendPasswordResetEmail(email) { success ->
                        android.util.Log.d("ForgotPasswordScreen", "Password reset result: success=$success")
                        if (success) {
                            emailSent = true
                        }
                    }
                },
                onBackToLogin = onNavigateBack
            )
        } else {
            EmailSentConfirmation(
                email = email,
                onBackToLogin = onNavigateBack
            )
        }
    }
}
