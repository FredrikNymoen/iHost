package no.ntnu.prog2007.ihost.ui.screens.auth.personalinfo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel

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
        Text(
            text = "Personal Information",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tell us a bit about yourself",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        // First name field
        OutlinedTextField(
            value = registrationState.firstName,
            onValueChange = { viewModel.updateRegistrationField("firstName", it) },
            label = { Text("First Name", color = MaterialTheme.colorScheme.onSurface) },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "First Name",
                    tint = MaterialTheme.colorScheme.onSurface
                )
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

        Spacer(modifier = Modifier.height(16.dp))

        // Last name field
        OutlinedTextField(
            value = registrationState.lastName,
            onValueChange = { viewModel.updateRegistrationField("lastName", it) },
            label = { Text("Last Name", color = MaterialTheme.colorScheme.onSurface) },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Last Name",
                    tint = MaterialTheme.colorScheme.onSurface
                )
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

        Spacer(modifier = Modifier.height(16.dp))

        // Username field
        OutlinedTextField(
            value = registrationState.username,
            onValueChange = {
                viewModel.updateRegistrationField("username", it)
                validateUsername(it)
            },
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
                text = usernameError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up button
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

        // Back button
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
