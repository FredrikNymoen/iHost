package no.ntnu.prog2007.ihost.data.repository

import android.util.Log
import no.ntnu.prog2007.ihost.data.model.Friendship
import no.ntnu.prog2007.ihost.data.remote.ApiService
import no.ntnu.prog2007.ihost.data.remote.FriendRequestRequest
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient

class FriendshipRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    /**
     * Send a friend request to another user
     */
    suspend fun sendFriendRequest(toUserId: String): Result<Friendship> {
        return try {
            val request = FriendRequestRequest(toUserId = toUserId)
            val friendship = apiService.sendFriendRequest(request)
            Log.d("FriendshipRepository", "Friend request sent: ${friendship.id}")
            Result.success(friendship)
        } catch (e: Exception) {
            Log.e("FriendshipRepository", "Error sending friend request", e)
            Result.failure(e)
        }
    }

    /**
     * Accept a friend request
     */
    suspend fun acceptFriendRequest(friendshipId: String): Result<Friendship> {
        return try {
            val friendship = apiService.acceptFriendRequest(friendshipId)
            Log.d("FriendshipRepository", "Friend request accepted: $friendshipId")
            Result.success(friendship)
        } catch (e: Exception) {
            Log.e("FriendshipRepository", "Error accepting friend request", e)
            Result.failure(e)
        }
    }

    /**
     * Decline a friend request
     */
    suspend fun declineFriendRequest(friendshipId: String): Result<Friendship> {
        return try {
            val friendship = apiService.declineFriendRequest(friendshipId)
            Log.d("FriendshipRepository", "Friend request declined: $friendshipId")
            Result.success(friendship)
        } catch (e: Exception) {
            Log.e("FriendshipRepository", "Error declining friend request", e)
            Result.failure(e)
        }
    }

    /**
     * Remove a friend (delete the friendship)
     */
    suspend fun removeFriend(friendshipId: String): Result<Unit> {
        return try {
            apiService.removeFriend(friendshipId)
            Log.d("FriendshipRepository", "Friend removed: $friendshipId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendshipRepository", "Error removing friend", e)
            Result.failure(e)
        }
    }

    /**
     * Get all pending friend requests (requests current user received)
     */
    suspend fun getPendingRequests(): Result<List<Friendship>> {
        return try {
            val friendships = apiService.getPendingRequests()
            Log.d("FriendshipRepository", "Loaded ${friendships.size} pending requests")
            Result.success(friendships)
        } catch (e: Exception) {
            Log.e("FriendshipRepository", "Error loading pending requests", e)
            Result.failure(e)
        }
    }

    /**
     * Get all friends (accepted friendships)
     */
    suspend fun getFriends(): Result<List<Friendship>> {
        return try {
            val friendships = apiService.getFriends()
            Log.d("FriendshipRepository", "Loaded ${friendships.size} friends")
            Result.success(friendships)
        } catch (e: Exception) {
            Log.e("FriendshipRepository", "Error loading friends", e)
            Result.failure(e)
        }
    }

    /**
     * Get all sent friend requests (requests current user sent that are still pending)
     */
    suspend fun getSentRequests(): Result<List<Friendship>> {
        return try {
            val friendships = apiService.getSentRequests()
            Log.d("FriendshipRepository", "Loaded ${friendships.size} sent requests")
            Result.success(friendships)
        } catch (e: Exception) {
            Log.e("FriendshipRepository", "Error loading sent requests", e)
            Result.failure(e)
        }
    }
}
