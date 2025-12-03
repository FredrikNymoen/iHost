package no.ntnu.prog2007.ihost.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.data.repository.UserRepository

data class UserUiState(
    val users: List<User> = emptyList(),
    val selectedUser: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUsernameAvailable: Boolean? = null,
    val isCheckingUsername: Boolean = false
)

/**
 * ViewModel for user profile operations
 * Handles user list, profile viewing, and profile updates
 */
class UserViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    /**
     * Load all users
     */
    fun loadAllUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            userRepository.getAllUsers().fold(
                onSuccess = { users ->
                    _uiState.update {
                        it.copy(
                            users = users,
                            isLoading = false
                        )
                    }
                    Log.d("UserViewModel", "Loaded ${users.size} users")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load users"
                        )
                    }
                    Log.e("UserViewModel", "Error loading users", error)
                }
            )
        }
    }

    /**
     * Load user profile by UID
     */
    fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            userRepository.getUserByUid(uid).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            selectedUser = user,
                            isLoading = false
                        )
                    }
                    Log.d("UserViewModel", "Loaded user profile: ${user.username}")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load user profile"
                        )
                    }
                    Log.e("UserViewModel", "Error loading user profile", error)
                }
            )
        }
    }

    /**
     * Check if username is available
     */
    fun checkUsernameAvailability(username: String) {
        if (username.isBlank()) {
            _uiState.update { it.copy(isUsernameAvailable = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUsername = true) }

            userRepository.isUsernameAvailable(username).fold(
                onSuccess = { available ->
                    _uiState.update {
                        it.copy(
                            isUsernameAvailable = available,
                            isCheckingUsername = false
                        )
                    }
                    Log.d("UserViewModel", "Username '$username' available: $available")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isCheckingUsername = false,
                            errorMessage = error.message ?: "Failed to check username"
                        )
                    }
                    Log.e("UserViewModel", "Error checking username", error)
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
        photoUrl: String? = null,
        phoneNumber: String? = null
    ) {
        viewModelScope.launch {
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
                            selectedUser = updatedUser,
                            isLoading = false
                        )
                    }
                    Log.d("UserViewModel", "User profile updated successfully")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to update profile"
                        )
                    }
                    Log.e("UserViewModel", "Error updating user profile", error)
                }
            )
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Clear selected user
     */
    fun clearSelectedUser() {
        _uiState.update { it.copy(selectedUser = null) }
    }
}
