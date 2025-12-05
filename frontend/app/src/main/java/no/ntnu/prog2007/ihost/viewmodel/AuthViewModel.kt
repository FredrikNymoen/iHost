package no.ntnu.prog2007.ihost.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import no.ntnu.prog2007.ihost.data.repository.AuthRepository
import no.ntnu.prog2007.ihost.data.repository.UserRepository

data class AuthUiState(
    val currentUser: FirebaseUser? = null,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registrationSuccess: Boolean = false,
    val isCheckingAuth: Boolean = true
)

/**
 * State holder for registration form fields
 */
data class RegistrationState(
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = ""
)

/**
 * ViewModel for authentication operations
 * Handles sign in, sign up, sign out, and authentication state
 */
class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val authRepository = AuthRepository(auth)
    private val userRepository = UserRepository()

    private val _registrationState = MutableStateFlow(RegistrationState())
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAndVerifyCurrentUser()
    }

    /**
     * Check if user is logged in and verify their auth token is valid
     */
    private fun checkAndVerifyCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAuth = true) }

            val user = auth.currentUser
            if (user != null) {
                try {
                    // Force token refresh to verify user still exists in Firebase
                    user.getIdToken(true).await()

                    // Token is valid, user is authenticated
                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            isLoggedIn = true,
                            isCheckingAuth = false
                        )
                    }
                    Log.d("AuthViewModel", "User authenticated: ${user.email}")
                } catch (e: Exception) {
                    // Token refresh failed - user no longer exists or token is invalid
                    Log.e("AuthViewModel", "Auth verification failed: ${e.message}", e)
                    auth.signOut()
                    _uiState.update {
                        it.copy(
                            currentUser = null,
                            isLoggedIn = false,
                            isCheckingAuth = false
                        )
                    }
                }
            } else {
                // No user logged in
                _uiState.update {
                    it.copy(
                        currentUser = null,
                        isLoggedIn = false,
                        isCheckingAuth = false
                    )
                }
            }
        }
    }

    /**
     * Update registration form fields
     */
    fun updateRegistrationField(field: String, value: String) {
        when (field) {
            "firstName" -> _registrationState.update { it.copy(firstName = value) }
            "lastName" -> _registrationState.update { it.copy(lastName = value) }
            "email" -> _registrationState.update { it.copy(email = value) }
            "password" -> _registrationState.update { it.copy(password = value) }
            "username" -> _registrationState.update { it.copy(username = value) }
        }
    }

    /**
     * Reset registration state
     */
    fun resetRegistrationState() {
        _registrationState.value = RegistrationState()
        _uiState.update { it.copy(registrationSuccess = false) }
    }

    /**
     * Clear registration success flag
     */
    fun clearRegistrationSuccess() {
        _uiState.update { it.copy(registrationSuccess = false) }
    }

    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.signIn(email, password)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isLoggedIn = true,
                        isLoading = false
                    )
                }
                Log.d("AuthViewModel", "User logged in: ${user.email}")
            }
            result.onFailure { e ->
                Log.e("AuthViewModel", "Sign in error: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        errorMessage = "Feil ved innlogging: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Sign up new user
     */
    fun signUp(username: String, password: String) {
        val regState = _registrationState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Check if username is available
            userRepository.isUsernameAvailable(username).fold(
                onSuccess = { isAvailable ->
                    if (!isAvailable) {
                        _uiState.update {
                            it.copy(
                                errorMessage = "Brukernavnet er allerede tatt.",
                                isLoading = false
                            )
                        }
                        return@launch
                    }

                    // Username is available - proceed with registration
                    viewModelScope.launch {
                        authRepository.registerUser(
                            username = username,
                            email = regState.email,
                            password = password,
                            firstName = regState.firstName,
                            lastName = regState.lastName
                        ).fold(
                            onSuccess = { user ->
                                _uiState.update {
                                    it.copy(
                                        currentUser = auth.currentUser,
                                        isLoggedIn = true,
                                        isLoading = false,
                                        registrationSuccess = true
                                    )
                                }
                                Log.d("AuthViewModel", "User signed up successfully: ${regState.email}")
                            },
                            onFailure = { e ->
                                Log.e("AuthViewModel", "Sign up error: ${e.message}", e)
                                _uiState.update {
                                    it.copy(
                                        errorMessage = "Feil ved registrering: ${e.localizedMessage}",
                                        isLoading = false,
                                        registrationSuccess = false
                                    )
                                }
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = "Feil ved sjekking av brukernavn: ${error.localizedMessage}",
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        authRepository.signOut()
        _uiState.update {
            it.copy(
                currentUser = null,
                isLoggedIn = false,
                errorMessage = null
            )
        }
        // Also clear registration state
        _registrationState.value = RegistrationState()
    }

    /**
     * Clear all auth data except current user state (call on logout)
     */
    fun clearAuthData() {
        _registrationState.value = RegistrationState()
        _uiState.update {
            it.copy(
                errorMessage = null,
                registrationSuccess = false,
                isLoading = false
            )
        }
    }

    /**
     * Check username availability (for registration validation)
     */
    fun checkUsernameAvailability(username: String, onResult: (Boolean) -> Unit) {
        if (username.isBlank() || username.length !in 4..12) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            userRepository.isUsernameAvailable(username).fold(
                onSuccess = { available -> onResult(available) },
                onFailure = { onResult(false) }
            )
        }
    }

    /**
     * Check email availability (for registration validation)
     */
    fun checkEmailAvailability(email: String, onResult: (Boolean) -> Unit) {
        if (email.isBlank()) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            userRepository.isEmailAvailable(email).fold(
                onSuccess = { available -> onResult(available) },
                onFailure = { onResult(false) }
            )
        }
    }

    /**
     * Send password reset email
     */
    fun sendPasswordResetEmail(email: String, onResult: (Boolean) -> Unit) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email") }
            onResult(false)
            return
        }

        viewModelScope.launch {
            Log.d("AuthViewModel", "sendPasswordResetEmail called with email: $email")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.sendPasswordResetEmail(email).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    Log.d("AuthViewModel", "Password reset email sent successfully to: $email")
                    onResult(true)
                },
                onFailure = { error ->
                    val errorMsg = when {
                        error.message?.contains("network", ignoreCase = true) == true ->
                            "Network error. Please check your internet connection."
                        error.message?.contains("user", ignoreCase = true) == true ->
                            "No account found with this email address."
                        else -> "Failed to send reset email: ${error.localizedMessage}"
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMsg
                        )
                    }
                    Log.e("AuthViewModel", "Error sending password reset email: ${error.message}", error)
                    onResult(false)
                }
            )
        }
    }
}
