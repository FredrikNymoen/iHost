package no.ntnu.prog2007.ihostapi.service.impl

import no.ntnu.prog2007.ihostapi.exception.ResourceNotFoundException
import no.ntnu.prog2007.ihostapi.model.entity.Friendship
import no.ntnu.prog2007.ihostapi.repository.FriendshipRepository
import no.ntnu.prog2007.ihostapi.service.FriendshipService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

/**
 * Service implementation for Friendship operations
 */
@Service
class FriendshipServiceImpl(
    private val friendshipRepository: FriendshipRepository
) : FriendshipService {
    private val logger = Logger.getLogger(FriendshipServiceImpl::class.java.name)

    /**
     * Send a friend request to another user
     * @param fromUserId The ID of the user sending the request
     * @param toUserId The ID of the user receiving the request
     * @return The created Friendship entity
     * @throws IllegalArgumentException if friendship already exists or user tries to befriend themselves
     */
    override fun sendFriendRequest(fromUserId: String, toUserId: String): Friendship {
        // Prevent sending request to self
        if (fromUserId == toUserId) {
            throw IllegalArgumentException("Cannot send friend request to yourself")
        }

        // Check if friendship already exists
        val existingFriendship = friendshipRepository.findByUsers(fromUserId, toUserId)
        if (existingFriendship != null) {
            throw IllegalArgumentException("Friendship request already exists")
        }

        // Create new friendship
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val timestamp = now.format(formatter)

        val savedFriendship = friendshipRepository.save(
            user1Id = fromUserId,
            user2Id = toUserId,
            status = "PENDING",
            requestedBy = fromUserId,
            requestedAt = timestamp,
            respondedAt = null
        )

        logger.info("Friend request sent from $fromUserId to $toUserId")
        return savedFriendship
    }

    /**
     * Accept a friend request
     * @param friendshipId The ID of the friendship
     * @param userId The ID of the user accepting the request
     * @return The updated Friendship entity with ACCEPTED status
     * @throws IllegalArgumentException if user is not the request recipient or friendship is not pending
     */
    override fun acceptFriendRequest(friendshipId: String, userId: String): Friendship {
        val friendship = friendshipRepository.findById(friendshipId)
            ?: throw ResourceNotFoundException("Friendship not found")

        // Verify current user is the recipient (user2)
        if (friendship.user2Id != userId) {
            throw IllegalArgumentException("You can only accept requests sent to you")
        }

        // Verify friendship is pending
        if (friendship.status != "PENDING") {
            throw IllegalArgumentException("Friendship is not pending")
        }

        // Update to ACCEPTED
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val timestamp = now.format(formatter)

        val updatedFriendship = friendship.copy(
            status = "ACCEPTED",
            respondedAt = timestamp
        )

        friendshipRepository.update(updatedFriendship)

        logger.info("Friend request accepted: $friendshipId")
        return updatedFriendship
    }

    /**
     * Decline a friend request
     * @param friendshipId The ID of the friendship
     * @param userId The ID of the user declining the request
     * @return The updated Friendship entity with DECLINED status
     * @throws IllegalArgumentException if user is not the request recipient or friendship is not pending
     */
    override fun declineFriendRequest(friendshipId: String, userId: String): Friendship {
        val friendship = friendshipRepository.findById(friendshipId)
            ?: throw ResourceNotFoundException("Friendship not found")

        // Verify current user is the recipient (user2)
        if (friendship.user2Id != userId) {
            throw IllegalArgumentException("You can only decline requests sent to you")
        }

        // Verify friendship is pending
        if (friendship.status != "PENDING") {
            throw IllegalArgumentException("Friendship is not pending")
        }

        // Update to DECLINED
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val timestamp = now.format(formatter)

        val updatedFriendship = friendship.copy(
            status = "DECLINED",
            respondedAt = timestamp
        )

        friendshipRepository.update(updatedFriendship)

        logger.info("Friend request declined: $friendshipId")
        return updatedFriendship
    }

    /**
     * Remove a friend (delete friendship)
     * @param friendshipId The ID of the friendship to remove
     * @param userId The ID of the user removing the friend
     * @throws IllegalArgumentException if user is not part of the friendship
     */
    override fun removeFriend(friendshipId: String, userId: String) {
        val friendship = friendshipRepository.findById(friendshipId)
            ?: throw ResourceNotFoundException("Friendship not found")

        // Verify current user is part of the friendship
        if (friendship.user1Id != userId && friendship.user2Id != userId) {
            throw IllegalArgumentException("You can only remove your own friendships")
        }

        friendshipRepository.delete(friendshipId)

        logger.info("Friendship removed: $friendshipId")
    }

    /**
     * Get pending friend requests received by a user
     * @param userId The ID of the user
     * @return List of pending Friendship entities where user is the recipient
     */
    override fun getPendingRequests(userId: String): List<Friendship> {
        val friendships = friendshipRepository.findPendingRequestsForUser(userId)
        logger.info("Retrieved ${friendships.size} pending requests for user: $userId")
        return friendships
    }

    /**
     * Get accepted friendships for a user
     * @param userId The ID of the user
     * @return List of accepted Friendship entities where user is a participant
     */
    override fun getFriends(userId: String): List<Friendship> {
        val friendships = friendshipRepository.findAcceptedFriendshipsForUser(userId)
        logger.info("Retrieved ${friendships.size} friends for user: $userId")
        return friendships
    }

    /**
     * Get pending friend requests sent by a user
     * @param userId The ID of the user
     * @return List of pending Friendship entities where user is the requester
     */
    override fun getSentRequests(userId: String): List<Friendship> {
        val friendships = friendshipRepository.findSentRequestsByUser(userId)
        logger.info("Retrieved ${friendships.size} sent requests for user: $userId")
        return friendships
    }
}
