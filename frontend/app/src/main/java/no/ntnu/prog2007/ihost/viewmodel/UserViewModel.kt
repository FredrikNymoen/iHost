package no.ntnu.prog2007.ihost.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.data.repository.ImageRepository
import no.ntnu.prog2007.ihost.data.repository.UserRepository

data class UserUiState(
    val users: List<User> = emptyList(),
    val selectedUser: User? = null,
    val isLoading: Boolean = false,
    val isProfileLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUsernameAvailable: Boolean? = null,
    val isCheckingUsername: Boolean = false,
    val lastUploadedPhotoUrl: String? = null
)

/**
 * ViewModel for user profile operations
 * Handles user list, profile viewing, profile updates, and profile photo uploads
 */
class UserViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val imageRepository = ImageRepository()

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()


    /**
     * Load user profile by UID
     */
    fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProfileLoading = true, errorMessage = null) }

            userRepository.getUserByUid(uid).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            selectedUser = user,
                            isProfileLoading = false
                        )
                    }
                    Log.d("UserViewModel", "Loaded user profile: ${user.username}")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProfileLoading = false,
                            errorMessage = error.message ?: "Failed to load user profile"
                        )
                    }
                    Log.e("UserViewModel", "Error loading user profile", error)
                }
            )
        }
    }

    /**
     * Update user profile
     */
    fun updateUserProfile(
        uid: String,
        firstName: String? = null,
        lastName: String? = null,
        photoUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProfileLoading = true, errorMessage = null) }

            userRepository.updateUserProfile(
                uid = uid,
                firstName = firstName,
                lastName = lastName,
                photoUrl = photoUrl
            ).fold(
                onSuccess = { updatedUser ->
                    _uiState.update {
                        it.copy(
                            selectedUser = updatedUser,
                            isProfileLoading = false
                        )
                    }
                    Log.d("UserViewModel", "User profile updated successfully")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProfileLoading = false,
                            errorMessage = error.message ?: "Failed to update profile"
                        )
                    }
                    Log.e("UserViewModel", "Error updating user profile", error)
                }
            )
        }
    }

    /**
     * Upload profile photo to backend
     * Returns the download URL if successful
     */
    suspend fun uploadProfilePhoto(context: Context, imageUri: Uri): String? {
        _uiState.update { it.copy(isProfileLoading = true, errorMessage = null) }

        return try {
            Log.d("UserViewModel", "Starting profile photo upload for URI: $imageUri")

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
                    Log.d("UserViewModel", "Profile photo uploaded successfully: $photoUrl")
                    _uiState.update {
                        it.copy(
                            isProfileLoading = false,
                            lastUploadedPhotoUrl = photoUrl
                        )
                    }
                    photoUrl
                },
                onFailure = { error ->
                    file.delete()
                    Log.e("UserViewModel", "Error uploading profile photo: ${error.message}", error)
                    _uiState.update {
                        it.copy(
                            isProfileLoading = false,
                            errorMessage = "Failed to upload photo: ${error.localizedMessage}"
                        )
                    }
                    null
                }
            )
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error preparing profile photo: ${e.message}", e)
            _uiState.update {
                it.copy(
                    isProfileLoading = false,
                    errorMessage = "Failed to upload photo: ${e.localizedMessage}"
                )
            }
            null
        }
    }

    /**
     * Clear all user data (call on logout)
     */
    fun clearUserData() {
        _uiState.value = UserUiState()
    }

    /**
     * Clear selected user
     */
    fun clearSelectedUser() {
        _uiState.update { it.copy(selectedUser = null) }
    }
}
