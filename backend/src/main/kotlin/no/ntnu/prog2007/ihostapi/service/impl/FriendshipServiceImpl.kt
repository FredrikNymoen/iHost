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

    override fun getPendingRequests(userId: String): List<Friendship> {
        val friendships = friendshipRepository.findPendingRequestsForUser(userId)
        logger.info("Retrieved ${friendships.size} pending requests for user: $userId")
        return friendships
    }

    override fun getFriends(userId: String): List<Friendship> {
        val friendships = friendshipRepository.findAcceptedFriendshipsForUser(userId)
        logger.info("Retrieved ${friendships.size} friends for user: $userId")
        return friendships
    }

    override fun getSentRequests(userId: String): List<Friendship> {
        val friendships = friendshipRepository.findSentRequestsByUser(userId)
        logger.info("Retrieved ${friendships.size} sent requests for user: $userId")
        return friendships
    }
}
