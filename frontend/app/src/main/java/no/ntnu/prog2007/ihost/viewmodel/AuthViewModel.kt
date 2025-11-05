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
import no.ntnu.prog2007.ihost.data.model.User
import no.ntnu.prog2007.ihost.data.repository.AuthRepository

data class AuthUiState(
    val currentUser: FirebaseUser? = null,
    val userProfile: User? = null, // Profile data to separate auth from user data
    val isProfileLoading: Boolean = false, // Loading state for profile data
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val authRepository = AuthRepository(auth)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val user = auth.currentUser
        _uiState.update { it.copy(currentUser = user, isLoggedIn = user != null) }
    }

    /**
     * Load user profile data from backend for the current authenticated user
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            // Fetch uid from the current authenticated user
            val uid = _uiState.value.currentUser?.uid ?: return@launch

            // Update loading state
            _uiState.update { it.copy(isProfileLoading = true) }

            try { // Call repository to get user profile
                val userResult = authRepository.getUserProfile(uid)
                userResult.onSuccess { profile -> // Success, update profile data
                    _uiState.update { it.copy(userProfile = profile, isProfileLoading = false) }
                }
                userResult.onFailure { error ->
                    _uiState.update { it.copy(isProfileLoading = false) }
                    Log.e("AuthViewModel", "Error fetching user profile: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProfileLoading = false) }
                Log.e("AuthViewModel", "Error loading user profile: ${e.message}", e)
            }
        }
    }

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

    fun signUp(name: String, email: String, password: String, firstName: String, lastName: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // Check if username is available
            val usernameResult = authRepository.isUsernameAvailable(username = name)
            if (usernameResult.isFailure) { // Error during check
                _uiState.update {
                    it.copy(
                        errorMessage = "Feil ved sjekking av brukernavn: ${usernameResult.exceptionOrNull()?.localizedMessage}",
                        isLoading = false
                    )
                }
                return@launch
            }
            val isAvailable = usernameResult.getOrNull() ?: false
            if (!isAvailable) { // Username taken
                _uiState.update {
                    it.copy(
                        errorMessage = "Brukernavnet er allerede tatt.",
                        isLoading = false
                    )
                }
                return@launch
            }

            // If we reach here, username is available - proceed with registration
            val result = authRepository.registerUser(name, email, password, firstName, lastName)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isLoggedIn = true,
                        isLoading = false
                    )
                }
                Log.d("AuthViewModel", "User signed up and registered: $email")
            }
            result.onFailure { e ->
                Log.e("AuthViewModel", "Sign up error: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        errorMessage = "Feil ved registrering: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.update { it.copy(currentUser = null, isLoggedIn = false) }
    }

    /**
     * Username availability check for SignUpScreen
     * Calls authRepositorys username availability check and returns result via callback
     * @param username The username to check
     * @param onResult Callback with result: true if available, false if taken or error
     */
    fun checkUsernameAvailability(username: String, onResult: (Boolean) -> Unit) {
        if (username.isBlank()) { // Empty username fail early
            onResult(false)
            return
        }
        if (username.length !in 4..12) { // Invalid length fail early
            onResult(false)
            return
        }
        viewModelScope.launch { // Call repository check
            try {
                val result = authRepository.isUsernameAvailable(username)
                result.onSuccess { available ->
                    onResult(available)
                }
                result.onFailure {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false) // We dont really care about why it failed, we just return that it failed.
            }
        }
    }

    suspend fun getIdToken(): String? {
        return authRepository.getIdToken()
    }
}
