package no.ntnu.prog2007.ihost.ui.screens.auth.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.ui.components.auth.AuthHeader
import no.ntnu.prog2007.ihost.ui.components.auth.ErrorMessageBox
import no.ntnu.prog2007.ihost.ui.components.auth.LoadingButton
import no.ntnu.prog2007.ihost.ui.components.auth.SecondaryButton
import no.ntnu.prog2007.ihost.ui.screens.auth.login.components.EmailTextField
import no.ntnu.prog2007.ihost.ui.screens.auth.login.components.PasswordTextField
import no.ntnu.prog2007.ihost.ui.screens.auth.login.components.ForgotPasswordLink
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthHeader(
            title = "Welcome back",
            subtitle = "Log in to continue"
        )

        Spacer(modifier = Modifier.height(32.dp))

        ErrorMessageBox(errorMessage = uiState.errorMessage)

        EmailTextField(
            value = email,
            onValueChange = { email = it },
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            enabled = !uiState.isLoading
        )

        ForgotPasswordLink(onClick = onNavigateToForgotPassword)

        Spacer(modifier = Modifier.height(16.dp))

        LoadingButton(
            onClick = { viewModel.signIn(email, password) },
            text = "Log In",
            isLoading = uiState.isLoading,
            enabled = email.isNotBlank() && password.isNotBlank()
        )

        Spacer(modifier = Modifier.height(16.dp))

        SecondaryButton(
            onClick = onNavigateBack,
            text = "Return"
        )
    }
}
