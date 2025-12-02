package no.ntnu.prog2007.ihostapi.service

import no.ntnu.prog2007.ihostapi.model.entity.Friendship

/**
 * Service interface for Friendship operations
 */
interface FriendshipService {
    fun sendFriendRequest(fromUserId: String, toUserId: String): Friendship
    fun acceptFriendRequest(friendshipId: String, userId: String): Friendship
    fun declineFriendRequest(friendshipId: String, userId: String): Friendship
    fun removeFriend(friendshipId: String, userId: String)
    fun getPendingRequests(userId: String): List<Friendship>
    fun getFriends(userId: String): List<Friendship>
    fun getSentRequests(userId: String): List<Friendship>
}
