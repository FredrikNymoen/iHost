package no.ntnu.prog2007.ihost.ui.screens.auth.signup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.ntnu.prog2007.ihost.ui.components.auth.AuthHeader
import no.ntnu.prog2007.ihost.ui.components.auth.ErrorMessageBox
import no.ntnu.prog2007.ihost.ui.screens.auth.signup.components.SignUpEmailTextField
import no.ntnu.prog2007.ihost.ui.screens.auth.signup.components.SignUpPasswordTextField
import no.ntnu.prog2007.ihost.ui.screens.auth.signup.components.PasswordMismatchError
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel

/**
 * User sign-up screen
 *
 * First step of the account creation flow where users enter their email and password.
 * Validates email uniqueness and password confirmation before allowing progression to
 * the PersonalInfoScreen.
 *
 * Features:
 * - Email input with real-time validation
 * - Password and confirm password inputs with visibility toggle
 * - Error display for mismatched passwords or existing email
 * - Navigation back to welcome or forward to personal info
 *
 * @param viewModel AuthViewModel for authentication operations
 * @param onNavigateBack Callback to return to Welcome screen
 * @param onNavigateToPersonalInfo Callback to navigate to personal info entry screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPersonalInfo: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val registrationState by viewModel.registrationState.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var hasAutoFilled by remember { mutableStateOf(false) }

    // Auto-fill confirm password ONCE if password is already set
    LaunchedEffect(Unit) {
        if (!hasAutoFilled && registrationState.password.isNotBlank() && confirmPassword.isEmpty()) {
            confirmPassword = registrationState.password
            hasAutoFilled = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthHeader(
            title = "Create Account",
            subtitle = "Enter your email and password"
        )

        Spacer(modifier = Modifier.height(32.dp))

        ErrorMessageBox(errorMessage = uiState.errorMessage)

        SignUpEmailTextField(
            value = registrationState.email,
            onValueChange = {
                viewModel.updateRegistrationField("email", it)
                emailError = null
            },
            emailError = emailError,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        SignUpPasswordTextField(
            value = registrationState.password,
            onValueChange = { viewModel.updateRegistrationField("password", it) },
            label = "Password",
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        SignUpPasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            passwordVisible = confirmPasswordVisible,
            onTogglePasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
            isError = confirmPassword.isNotEmpty() && registrationState.password != confirmPassword,
            enabled = !uiState.isLoading
        )

        PasswordMismatchError(
            show = confirmPassword.isNotEmpty() && registrationState.password != confirmPassword
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.checkEmailAvailability(registrationState.email) { available ->
                    if (available) {
                        onNavigateToPersonalInfo()
                    } else {
                        emailError = "This email is already registered"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !uiState.isLoading &&
                    registrationState.email.isNotBlank() &&
                    registrationState.password.isNotBlank() &&
                    registrationState.password == confirmPassword
        ) {
            Text("Next", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Return", fontSize = 16.sp)
        }
    }
}
