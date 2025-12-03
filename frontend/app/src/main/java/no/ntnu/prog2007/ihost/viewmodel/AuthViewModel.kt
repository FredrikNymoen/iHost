package no.ntnu.prog2007.ihost.viewmodel

import android.content.Context
import android.net.Uri
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
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.data.repository.AuthRepository
import no.ntnu.prog2007.ihost.data.repository.UserRepository
import no.ntnu.prog2007.ihost.data.repository.ImageRepository

data class AuthUiState(
    val currentUser: FirebaseUser? = null,
    val userProfile: User? = null, // Profile data to separate auth from user data
    val isProfileLoading: Boolean = false, // Loading state for profile data
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * State holder for registration form fields for the personal info part of the registration form
 */
data class RegistrationState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
)

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val authRepository = AuthRepository(auth)
    private val userRepository = UserRepository()
    private val imageRepository = ImageRepository()
    private val _registrationState = MutableStateFlow(RegistrationState()) // State for registration form
    val registrationState : StateFlow<RegistrationState> = _registrationState.asStateFlow() // read only expose of registration state

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    /**
     * Update registration form fields in the registration state
     */
    fun updateRegistrationField(field: String, value: String) {
        when (field) {
            "firstName" -> _registrationState.update { it.copy(firstName = value) }
            "lastName" -> _registrationState.update { it.copy(lastName = value) }
            "email" -> _registrationState.update { it.copy(email = value) }
        }
    }

    /**
     * Reset registration state
     */
    fun resetRegistrationState() {
        _registrationState.value = RegistrationState()
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

            userRepository.getUserByUid(uid).fold(
                onSuccess = { profile ->
                    _uiState.update { it.copy(userProfile = profile, isProfileLoading = false) }
                },
                onFailure = { error ->
                    // Check if its a 404 not found error to handle deleted or missing profiles
                    if (error.message?.contains("404") == true ||
                        error.message?.contains("not found") == true) {

                        // Profile doesnt exist, force log out and inform
                        Log.w("AuthViewModel","User profile not found for authenticated user $uid, signing out.")
                        signOut()
                        _uiState.update {
                            it.copy(
                                isProfileLoading = false,
                                errorMessage = "Your account has been deleted or does not exist. Please sign in again."
                            )
                        }
                    } else {
                        // Other error, just update load state
                        _uiState.update { it.copy(isProfileLoading = false) }
                        Log.e("AuthViewModel", "Error fetching user profile: ${error.message}")
                    }
                }
            )
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

    fun signUp(username: String, password: String) {
        val regState = _registrationState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Check if username is available using UserRepository
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
                                        currentUser = user,
                                        isLoggedIn = true,
                                        isLoading = false
                                    )
                                }
                                Log.d("AuthViewModel", "User signed up and registered: ${regState.email}")
                            },
                            onFailure = { e ->
                                Log.e("AuthViewModel", "Sign up error: ${e.message}", e)
                                _uiState.update {
                                    it.copy(
                                        errorMessage = "Feil ved registrering: ${e.localizedMessage}",
                                        isLoading = false
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

    fun signOut() {
        authRepository.signOut()
        _uiState.update { it.copy(currentUser = null, isLoggedIn = false) }
    }

    /**
     * Username availability check for SignUpScreen
     * Calls UserRepository's username availability check and returns result via callback
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
        viewModelScope.launch {
            userRepository.isUsernameAvailable(username).fold(
                onSuccess = { available ->
                    onResult(available)
                },
                onFailure = {
                    onResult(false)
                }
            )
        }
    }

    suspend fun getIdToken(): String? {
        return authRepository.getIdToken()
    }

    /**
     * Upload a profile photo to Cloudinary
     * @param context Android context for accessing content resolver
     * @param imageUri URI of the image to upload
     * @return The Cloudinary URL of the uploaded image, or null if upload fails
     */
    suspend fun uploadProfilePhoto(context: Context, imageUri: Uri): String? {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        return try {
            Log.d("AuthViewModel", "Starting profile photo upload for URI: $imageUri")

            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw IllegalArgumentException("Cannot open image URI")

            val file = java.io.File(context.cacheDir, "profile_upload_${System.currentTimeMillis()}.jpg")
            java.io.FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            imageRepository.uploadProfilePhoto(file).fold(
                onSuccess = { photoUrl ->
                    file.delete()
                    Log.d("AuthViewModel", "Profile photo uploaded successfully: $photoUrl")
                    _uiState.update { it.copy(isLoading = false) }
                    photoUrl
                },
                onFailure = { error ->
                    file.delete()
                    Log.e("AuthViewModel", "Error uploading profile photo: ${error.message}", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to upload photo: ${error.localizedMessage}"
                        )
                    }
                    null
                }
            )
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error preparing profile photo: ${e.message}", e)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Failed to upload photo: ${e.localizedMessage}"
                )
            }
            null
        }
    }

    /**
     * Update user profile (firstName, lastName, photoUrl, phoneNumber)
     * @param firstName New first name (optional)
     * @param lastName New last name (optional)
     * @param photoUrl New photo URL (optional)
     * @param phoneNumber New phone number (optional)
     */
    fun updateUserProfile(
        firstName: String? = null,
        lastName: String? = null,
        photoUrl: String? = null,
        phoneNumber: String? = null
    ) {
        viewModelScope.launch {
            val uid = _uiState.value.currentUser?.uid ?: return@launch
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            userRepository.updateUserProfile(
                uid = uid,
                firstName = firstName,
                lastName = lastName,
                photoUrl = photoUrl,
                phoneNumber = phoneNumber
            ).fold(
                onSuccess = { updatedUser ->
                    _uiState.update {
                        it.copy(
                            userProfile = updatedUser,
                            isLoading = false
                        )
                    }
                    Log.d("AuthViewModel", "User profile updated successfully")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to update profile: ${error.localizedMessage}"
                        )
                    }
                    Log.e("AuthViewModel", "Error updating profile: ${error.message}")
                }
            )
        }
    }
}
