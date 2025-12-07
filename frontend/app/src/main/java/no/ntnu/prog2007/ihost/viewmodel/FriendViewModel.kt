package no.ntnu.prog2007.ihost.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import no.ntnu.prog2007.ihost.data.model.domain.Friendship
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.data.repository.AuthRepository
import no.ntnu.prog2007.ihost.data.repository.FriendshipRepository
import no.ntnu.prog2007.ihost.data.repository.UserRepository

/**
 * UI state for friendship-related screens
 *
 * @property friends List of accepted friendships for the current user
 * @property pendingRequests List of incoming friend requests awaiting acceptance
 * @property sentRequests List of outgoing friend requests sent by current user
 * @property allUsers List of all users in the app (for Add Friend feature)
 * @property userDetailsMap Map of user IDs to User objects for displaying friend details
 * @property isLoading Indicates if an operation is in progress
 * @property errorMessage Error message to display, or null if no error
 */
data class FriendUiState(
    val friends: List<Friendship> = emptyList(),
    val pendingRequests: List<Friendship> = emptyList(),
    val sentRequests: List<Friendship> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val userDetailsMap: Map<String, User> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for managing friendship operations
 *
 * Handles friend requests, friendships, and user discovery.
 * Maintains friendship state across the application using StateFlow.
 *
 * Key responsibilities:
 * - Loading and managing friend lists
 * - Sending, accepting, and declining friend requests
 * - Removing friendships
 * - Loading user details for displaying friend information
 * - Managing the "Add Friend" user discovery feature
 */
class FriendViewModel : ViewModel() {
    private val authRepository = AuthRepository(FirebaseAuth.getInstance())
    private val friendshipRepository = FriendshipRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(FriendUiState())
    val uiState: StateFlow<FriendUiState> = _uiState.asStateFlow()

    /**
     * Clear all friendship data (call on logout)
     */
    fun clearFriendships() {
        _uiState.value = FriendUiState()
    }

    /**
     * Load friends, pending requests, and sent requests for current user
     */
    fun loadFriendships() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Load friends
                val friendsResult = friendshipRepository.getFriends()
                friendsResult.onSuccess { friendships ->
                    _uiState.update { it.copy(friends = friendships) }
                    loadUserDetails(friendships)
                }

                // Load pending requests
                val pendingResult = friendshipRepository.getPendingRequests()
                pendingResult.onSuccess { requests ->
                    _uiState.update { it.copy(pendingRequests = requests) }
                    loadUserDetails(requests)
                }

                // Load sent requests
                val sentResult = friendshipRepository.getSentRequests()
                sentResult.onSuccess { sent ->
                    _uiState.update { it.copy(sentRequests = sent) }
                    loadUserDetails(sent)
                }

                // Check if any failed
                if (friendsResult.isFailure || pendingResult.isFailure || sentResult.isFailure) {
                    val error = friendsResult.exceptionOrNull()
                        ?: pendingResult.exceptionOrNull()
                        ?: sentResult.exceptionOrNull()
                    throw error ?: Exception("Unknown error loading friendships")
                }
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error loading friendships", e)
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to load friendships: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Load user details for friendships
     *
     * Fetches User objects for all users involved in the provided friendships
     * (excluding the current user) and caches them in userDetailsMap.
     * This enables displaying friend names and profile information in the UI.
     *
     * @param friendships List of friendships to load user details for
     */
    private fun loadUserDetails(friendships: List<Friendship>) {
        viewModelScope.launch {
            val currentUserId = authRepository.getCurrentUser()?.uid ?: return@launch
            val userIds = friendships.flatMap {
                listOf(it.user1Id, it.user2Id)
            }.distinct().filter { it != currentUserId }

            val updatedMap = _uiState.value.userDetailsMap.toMutableMap()

            userIds.forEach { userId ->
                if (!updatedMap.containsKey(userId)) {
                    userRepository.getUserByUid(userId).fold(
                        onSuccess = { user ->
                            updatedMap[userId] = user
                        },
                        onFailure = { error ->
                            Log.e("FriendViewModel", "Error loading user $userId: ${error.message}", error)
                        }
                    )
                }
            }

            _uiState.update { it.copy(userDetailsMap = updatedMap) }
        }
    }

    /**
     * Load all users in the app (for Add Friend screen)
     */
    fun loadAllUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            userRepository.getAllUsers().fold(
                onSuccess = { users ->
                    val currentUserId = authRepository.getCurrentUser()?.uid

                    // Filter out current user
                    val filteredUsers = users.filter { it.uid != currentUserId }

                    _uiState.update {
                        it.copy(
                            allUsers = filteredUsers,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    Log.e("FriendViewModel", "Error loading all users: ${error.message}", error)
                    _uiState.update {
                        it.copy(
                            errorMessage = "Failed to load users: ${error.localizedMessage}",
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    /**
     * Send a friend request to another user
     *
     * Creates a new friendship with PENDING status. Reloads friendships
     * on success to update the UI.
     *
     * @param toUserId The UID of the user to send the request to
     * @param onSuccess Callback invoked on successful request
     * @param onError Callback invoked on error with error message
     */
    fun sendFriendRequest(toUserId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = friendshipRepository.sendFriendRequest(toUserId)
                result.onSuccess { friendship ->
                    _uiState.update { it.copy(isLoading = false) }
                    // Reload sent requests to show the new request
                    loadFriendships()
                    onSuccess()
                }
                result.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.localizedMessage,
                            isLoading = false
                        )
                    }
                    onError(error.localizedMessage ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error sending friend request", e)
                _uiState.update {
                    it.copy(
                        errorMessage = e.localizedMessage,
                        isLoading = false
                    )
                }
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Accept a friend request
     *
     * Updates the friendship status to ACCEPTED. Reloads friendships
     * to move the friendship from pending to accepted list.
     *
     * @param friendshipId The ID of the friendship document
     * @param onSuccess Callback invoked on successful acceptance
     * @param onError Callback invoked on error with error message
     */
    fun acceptFriendRequest(friendshipId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = friendshipRepository.acceptFriendRequest(friendshipId)
                result.onSuccess {
                    // Reload friendships to update UI
                    loadFriendships()
                    onSuccess()
                }
                result.onFailure { error ->
                    onError(error.localizedMessage ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error accepting friend request", e)
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Decline a friend request
     *
     * Deletes the friendship document. Reloads friendships to
     * remove the request from the UI.
     *
     * @param friendshipId The ID of the friendship document
     * @param onSuccess Callback invoked on successful decline
     * @param onError Callback invoked on error with error message
     */
    fun declineFriendRequest(friendshipId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = friendshipRepository.declineFriendRequest(friendshipId)
                result.onSuccess {
                    // Reload friendships to update UI
                    loadFriendships()
                    onSuccess()
                }
                result.onFailure { error ->
                    onError(error.localizedMessage ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error declining friend request", e)
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Remove a friend
     *
     * Deletes an accepted friendship. Reloads friendships to
     * remove the friend from the list.
     *
     * @param friendshipId The ID of the friendship document
     * @param onSuccess Callback invoked on successful removal
     * @param onError Callback invoked on error with error message
     */
    fun removeFriend(friendshipId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = friendshipRepository.removeFriend(friendshipId)
                result.onSuccess {
                    // Reload friendships to update UI
                    loadFriendships()
                    onSuccess()
                }
                result.onFailure { error ->
                    onError(error.localizedMessage ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error removing friend", e)
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }


}
