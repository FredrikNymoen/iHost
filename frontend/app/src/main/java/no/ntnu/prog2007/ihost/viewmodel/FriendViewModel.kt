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

data class FriendUiState(
    val friends: List<Friendship> = emptyList(),
    val pendingRequests: List<Friendship> = emptyList(),
    val sentRequests: List<Friendship> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val userDetailsMap: Map<String, User> = emptyMap(), // Map userId -> User details
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

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
