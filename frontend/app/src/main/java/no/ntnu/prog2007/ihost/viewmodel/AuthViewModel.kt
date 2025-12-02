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
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient
import no.ntnu.prog2007.ihost.data.repository.AuthRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

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

            try { // Call repository to get user profile
                val userResult = authRepository.getUserProfile(uid)
                userResult.onSuccess { profile -> // Success, update profile data
                    _uiState.update { it.copy(userProfile = profile, isProfileLoading = false) }
                }
                userResult.onFailure { error ->
                    // Check if its a 404 not found error to handle deleted or missing profiles trying to be loaded from stored auth tokens
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

    fun signUp(username: String, password: String) {
        val regState = _registrationState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // Check if username is available
            val usernameResult = authRepository.isUsernameAvailable(username = username)
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
            val result = authRepository.registerUser(
                username = username,
                email = regState.email,
                password = password,
                firstName = regState.firstName,
                lastName = regState.lastName
            )
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isLoggedIn = true,
                        isLoading = false
                    )
                }
                Log.d("AuthViewModel", "User signed up and registered: $regState.email")
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

            // Get input stream from URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw IllegalArgumentException("Cannot open image URI")

            // Create a temporary file
            val file = File(context.cacheDir, "profile_upload_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            // Create multipart request body
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // Upload to backend
            val response = RetrofitClient.apiService.uploadProfilePhoto(body)

            // Clean up temporary file
            file.delete()

            Log.d("AuthViewModel", "Profile photo uploaded successfully: ${response.photoUrl}")
            _uiState.update { it.copy(isLoading = false) }
            response.photoUrl
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error uploading profile photo: ${e.message}", e)
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

            try {
                val result = authRepository.updateUserProfile(
                    uid = uid,
                    firstName = firstName,
                    lastName = lastName,
                    photoUrl = photoUrl,
                    phoneNumber = phoneNumber
                )

                result.onSuccess { updatedUser ->
                    _uiState.update {
                        it.copy(
                            userProfile = updatedUser,
                            isLoading = false
                        )
                    }
                    Log.d("AuthViewModel", "User profile updated successfully")
                }

                result.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to update profile: ${error.localizedMessage}"
                        )
                    }
                    Log.e("AuthViewModel", "Error updating profile: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error updating profile: ${e.localizedMessage}"
                    )
                }
                Log.e("AuthViewModel", "Exception updating profile: ${e.message}", e)
            }
        }
    }
}
