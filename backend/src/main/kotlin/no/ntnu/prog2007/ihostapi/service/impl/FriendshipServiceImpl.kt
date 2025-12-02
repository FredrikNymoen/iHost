package no.ntnu.prog2007.ihostapi.service.impl

import com.google.cloud.firestore.Firestore
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
    private val friendshipRepository: FriendshipRepository,
    private val firestore: Firestore
) : FriendshipService {
    private val logger = Logger.getLogger(FriendshipServiceImpl::class.java.name)

    override fun sendFriendRequest(fromUserId: String, toUserId: String): Friendship {
        // Prevent sending request to self
        if (fromUserId == toUserId) {
            throw IllegalArgumentException("Cannot send friend request to yourself")
        }

        // Check if friendship already exists
        val existingFriendship = checkExistingFriendship(fromUserId, toUserId)
        if (existingFriendship != null) {
            throw IllegalArgumentException("Friendship request already exists")
        }

        // Create new friendship
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val timestamp = now.format(formatter)

        val friendshipData = mapOf(
            "user1Id" to fromUserId,
            "user2Id" to toUserId,
            "status" to "PENDING",
            "requestedBy" to fromUserId,
            "requestedAt" to timestamp,
            "respondedAt" to null
        )

        // Save to Firestore (without id field)
        val docRef = firestore.collection(FriendshipRepository.COLLECTION_NAME)
            .add(friendshipData)
            .get()

        val createdFriendship = Friendship(
            id = docRef.id,
            user1Id = fromUserId,
            user2Id = toUserId,
            status = "PENDING",
            requestedBy = fromUserId,
            requestedAt = timestamp,
            respondedAt = null
        )

        logger.info("Friend request sent from $fromUserId to $toUserId")
        return createdFriendship
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

        val updateData = mapOf(
            "user1Id" to friendship.user1Id,
            "user2Id" to friendship.user2Id,
            "status" to "ACCEPTED",
            "requestedBy" to friendship.requestedBy,
            "requestedAt" to friendship.requestedAt,
            "respondedAt" to timestamp
        )

        firestore.collection(FriendshipRepository.COLLECTION_NAME)
            .document(friendshipId)
            .set(updateData)
            .get()

        val updatedFriendship = Friendship(
            id = friendshipId,
            user1Id = friendship.user1Id,
            user2Id = friendship.user2Id,
            status = "ACCEPTED",
            requestedBy = friendship.requestedBy,
            requestedAt = friendship.requestedAt,
            respondedAt = timestamp
        )

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

        val updateData = mapOf(
            "user1Id" to friendship.user1Id,
            "user2Id" to friendship.user2Id,
            "status" to "DECLINED",
            "requestedBy" to friendship.requestedBy,
            "requestedAt" to friendship.requestedAt,
            "respondedAt" to timestamp
        )

        firestore.collection(FriendshipRepository.COLLECTION_NAME)
            .document(friendshipId)
            .set(updateData)
            .get()

        val updatedFriendship = Friendship(
            id = friendshipId,
            user1Id = friendship.user1Id,
            user2Id = friendship.user2Id,
            status = "DECLINED",
            requestedBy = friendship.requestedBy,
            requestedAt = friendship.requestedAt,
            respondedAt = timestamp
        )

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

        // Delete the friendship
        firestore.collection(FriendshipRepository.COLLECTION_NAME)
            .document(friendshipId)
            .delete()
            .get()

        logger.info("Friendship removed: $friendshipId")
    }

    override fun getPendingRequests(userId: String): List<Friendship> {
        // Query friendships where current user is user2 and status is PENDING
        val query = firestore.collection(FriendshipRepository.COLLECTION_NAME)
            .whereEqualTo("user2Id", userId)
            .whereEqualTo("status", "PENDING")
            .get()
            .get()

        val friendships = query.documents.mapNotNull { doc ->
            doc.toObject(Friendship::class.java)?.copy(id = doc.id)
        }

        logger.info("Retrieved ${friendships.size} pending requests for user: $userId")
        return friendships
    }

    override fun getFriends(userId: String): List<Friendship> {
        // Query friendships where current user is user1 and status is ACCEPTED
        val query1 = firestore.collection(FriendshipRepository.COLLECTION_NAME)
            .whereEqualTo("user1Id", userId)
            .whereEqualTo("status", "ACCEPTED")
            .get()
            .get()

        val friendships1 = query1.documents.mapNotNull { doc ->
            doc.toObject(Friendship::class.java)?.copy(id = doc.id)
        }

        // Query friendships where current user is user2 and status is ACCEPTED
        val query2 = firestore.collection(FriendshipRepository.COLLECTION_NAME)
            .whereEqualTo("user2Id", userId)
            .whereEqualTo("status", "ACCEPTED")
            .get()
            .get()

        val friendships2 = query2.documents.mapNotNull { doc ->
            doc.toObject(Friendship::class.java)?.copy(id = doc.id)
        }

        val allFriendships = friendships1 + friendships2

        logger.info("Retrieved ${allFriendships.size} friends for user: $userId")
        return allFriendships
    }

    override fun getSentRequests(userId: String): List<Friendship> {
        // Query friendships where current user is user1 and status is PENDING
        val query = firestore.collection(FriendshipRepository.COLLECTION_NAME)
            .whereEqualTo("user1Id", userId)
            .whereEqualTo("status", "PENDING")
            .get()
            .get()

        val friendships = query.documents.mapNotNull { doc ->
            doc.toObject(Friendship::class.java)?.copy(id = doc.id)
        }

        logger.info("Retrieved ${friendships.size} sent requests for user: $userId")
        return friendships
    }

    /**
     * Helper function to check if a friendship already exists between two users
     */
    private fun checkExistingFriendship(userId1: String, userId2: String): Friendship? {
        return try {
            // Check user1 -> user2
            val query1 = firestore.collection(FriendshipRepository.COLLECTION_NAME)
                .whereEqualTo("user1Id", userId1)
                .whereEqualTo("user2Id", userId2)
                .limit(1)
                .get()
                .get()

            if (!query1.isEmpty) {
                return query1.documents.first().toObject(Friendship::class.java)
                    ?.copy(id = query1.documents.first().id)
            }

            // Check user2 -> user1
            val query2 = firestore.collection(FriendshipRepository.COLLECTION_NAME)
                .whereEqualTo("user1Id", userId2)
                .whereEqualTo("user2Id", userId1)
                .limit(1)
                .get()
                .get()

            if (!query2.isEmpty) {
                return query2.documents.first().toObject(Friendship::class.java)
                    ?.copy(id = query2.documents.first().id)
            }

            null
        } catch (e: Exception) {
            logger.warning("Error checking existing friendship: ${e.message}")
            null
        }
    }
}
