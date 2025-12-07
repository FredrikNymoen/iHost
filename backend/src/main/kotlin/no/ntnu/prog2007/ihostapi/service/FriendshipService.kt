package no.ntnu.prog2007.ihostapi.service

import no.ntnu.prog2007.ihostapi.model.entity.Friendship

/**
 * Service interface for Friendship operations
 */
interface FriendshipService {
    /**
     * Send a friend request to another user
     * @param fromUserId The ID of the user sending the request
     * @param toUserId The ID of the user receiving the request
     * @return The created Friendship entity
     * @throws IllegalArgumentException if friendship already exists or user tries to befriend themselves
     */
    fun sendFriendRequest(fromUserId: String, toUserId: String): Friendship

    /**
     * Accept a friend request
     * @param friendshipId The ID of the friendship
     * @param userId The ID of the user accepting the request
     * @return The updated Friendship entity with ACCEPTED status
     * @throws IllegalArgumentException if user is not the request recipient or friendship is not pending
     */
    fun acceptFriendRequest(friendshipId: String, userId: String): Friendship

    /**
     * Decline a friend request
     * @param friendshipId The ID of the friendship
     * @param userId The ID of the user declining the request
     * @return The updated Friendship entity with DECLINED status
     * @throws IllegalArgumentException if user is not the request recipient or friendship is not pending
     */
    fun declineFriendRequest(friendshipId: String, userId: String): Friendship

    /**
     * Remove a friend (delete friendship)
     * @param friendshipId The ID of the friendship to remove
     * @param userId The ID of the user removing the friend
     * @throws IllegalArgumentException if user is not part of the friendship
     */
    fun removeFriend(friendshipId: String, userId: String)

    /**
     * Get pending friend requests received by a user
     * @param userId The ID of the user
     * @return List of pending Friendship entities where user is the recipient
     */
    fun getPendingRequests(userId: String): List<Friendship>

    /**
     * Get accepted friendships for a user
     * @param userId The ID of the user
     * @return List of accepted Friendship entities where user is a participant
     */
    fun getFriends(userId: String): List<Friendship>

    /**
     * Get pending friend requests sent by a user
     * @param userId The ID of the user
     * @return List of pending Friendship entities where user is the requester
     */
    fun getSentRequests(userId: String): List<Friendship>
}
