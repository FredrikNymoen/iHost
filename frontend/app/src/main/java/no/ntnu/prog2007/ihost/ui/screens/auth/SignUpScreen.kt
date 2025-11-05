package no.ntnu.prog2007.ihost.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    // State variables
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Input field states
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Username validation state
    var isCheckingUsername by remember { mutableStateOf(false) }
    var isUsernameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }

    // Username availability check debounce to run only after user stops typing so we dont
    // accidentally spam the backend by checking on every keystroke
    val scope = rememberCoroutineScope()
    val debouncePeriod = 500L // milliseconds
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    // Username validation function
    fun validateUsername(value: String) {
        // Reset previous errores and states first
        isUsernameAvailable = null
        usernameError = null

        // If its blank, no need to check
        if (value.isBlank()) return

        // Check length
        if (value.length !in 4..12) {
            usernameError = "Brukernavn må være mellom 4 og 12 tegn"
            return
        }

        // Check for illegal characters
        if (!value.matches(Regex("^[a-zA-z0-9_]+$"))) {
            usernameError = "Brukernavn kan kun inneholde bokstaver, tall og understrek"
            return
        }

        // Cancel previous debounce job if any
        debounceJob?.cancel()

        // Launch new debounce job and set checking state to true
        isCheckingUsername = true
        debounceJob = scope.launch {
            delay(debouncePeriod) // Wait for user to stop typing
            // Call the viewModel to check username availability
            viewModel.checkUsernameAvailability(value) { available ->
                isCheckingUsername = false // Done checking
                isUsernameAvailable = available // Update availability state
                // Set error message if not available
                usernameError = if (!available) "Brukernavnet er allerede tatt" else null
            }

        }
    }

    // Focus requesters
    val nameFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Opprett konto",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFC107)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registrer deg for å begynne",
            fontSize = 16.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Error message
        if (uiState.errorMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }

        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                validateUsername(it)
            },
            label = { Text("Brukernavn", color = Color.White) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username", tint = Color.White) },
            trailingIcon = { // Show loading, check or error icon based on state
                when {
                    isCheckingUsername -> { // Show loading indicator
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    }
                    isUsernameAvailable == true -> { // Green check icon if username is available
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Brukernavn tilgjengelig",
                            tint = Color.Green
                        )
                    }
                    isUsernameAvailable == false -> { // Red close icon if username is taken
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Brukernavn ikke tilgjengelig",
                            tint = Color.Red
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(nameFocusRequester),
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        // Username error
        if (usernameError != null) {
            Text(
                text= usernameError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-post", color = Color.White) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.White) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(emailFocusRequester),
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Passord", color = Color.White) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color.White) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Skjul passord" else "Vis passord",
                        tint = Color.White
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester),
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Bekreft passord", color = Color.White) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password", tint = Color.White) },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Skjul passord" else "Vis passord",
                        tint = Color.White
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = confirmPassword.isNotEmpty() && password != confirmPassword,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(confirmPasswordFocusRequester),
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        if (confirmPassword.isNotEmpty() && password != confirmPassword) {
            Text(
                text = "Passordene stemmer ikke overens",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign up button
        Button(
            onClick = { viewModel.signUp(username, email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !uiState.isLoading &&
                    username.isNotBlank() &&
                    email.isNotBlank() &&
                    password.isNotBlank() &&
                    password == confirmPassword
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Registrer deg", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login link
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Har du allerede en konto?",
                color = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text("Logg inn", color = Color(0xFFFFC107))
            }
        }
    }
}
