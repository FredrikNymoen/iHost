package no.ntnu.prog2007.ihost.ui.screens.auth.personalinfo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.ntnu.prog2007.ihost.ui.components.auth.AuthHeader
import no.ntnu.prog2007.ihost.ui.screens.auth.personalinfo.components.NameTextField
import no.ntnu.prog2007.ihost.ui.screens.auth.personalinfo.components.UsernameTextField
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel

/**
 * Personal information screen
 *
 * Second step of the account creation flow where users provide their name and username.
 * Validates username availability in real-time with 500ms debounce and enforces format
 * requirements (4-12 characters, alphanumeric and underscores only).
 *
 * Features:
 * - First name and last name inputs
 * - Username with real-time availability checking
 * - Format validation (length, character constraints)
 * - Error display for validation failures
 * - Automatic redirect on successful account creation
 *
 * @param viewModel AuthViewModel for authentication operations
 * @param onSignUp Callback invoked after successful account creation
 * @param onNavigateBack Callback to return to SignUp screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    viewModel: AuthViewModel,
    onSignUp: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val registrationState by viewModel.registrationState.collectAsState()

    // Navigate when registration is successful
    LaunchedEffect(uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            viewModel.clearRegistrationSuccess()
            onSignUp()
        }
    }

    // Username validation state
    var isCheckingUsername by remember { mutableStateOf(false) }
    var isUsernameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }

    // Username availability check debounce
    val scope = rememberCoroutineScope()
    val debouncePeriod = 500L
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    // Username validation function
    fun validateUsername(value: String) {
        isUsernameAvailable = null
        usernameError = null

        if (value.isBlank()) return

        if (value.length !in 4..12) {
            usernameError = "Username must be between 4 and 12 characters"
            return
        }

        if (!value.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            usernameError = "Username can only contain letters, numbers and underscores"
            return
        }

        debounceJob?.cancel()
        isCheckingUsername = true
        debounceJob = scope.launch {
            delay(debouncePeriod)
            viewModel.checkUsernameAvailability(value) { available ->
                isCheckingUsername = false
                isUsernameAvailable = available
                usernameError = if (!available) "Username is already taken" else null
            }
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
            title = "Personal Information",
            subtitle = "Tell us a bit about yourself"
        )

        Spacer(modifier = Modifier.height(32.dp))

        NameTextField(
            value = registrationState.firstName,
            onValueChange = { viewModel.updateRegistrationField("firstName", it) },
            label = "First Name"
        )

        Spacer(modifier = Modifier.height(16.dp))

        NameTextField(
            value = registrationState.lastName,
            onValueChange = { viewModel.updateRegistrationField("lastName", it) },
            label = "Last Name"
        )

        Spacer(modifier = Modifier.height(16.dp))

        UsernameTextField(
            value = registrationState.username,
            onValueChange = {
                viewModel.updateRegistrationField("username", it)
                validateUsername(it)
            },
            isCheckingUsername = isCheckingUsername,
            isUsernameAvailable = isUsernameAvailable,
            usernameError = usernameError
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.signUp(registrationState.username, registrationState.password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !uiState.isLoading &&
                    registrationState.firstName.isNotBlank() &&
                    registrationState.lastName.isNotBlank() &&
                    registrationState.username.isNotBlank() &&
                    isUsernameAvailable == true
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign Up", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Back", fontSize = 16.sp)
        }
    }
}
