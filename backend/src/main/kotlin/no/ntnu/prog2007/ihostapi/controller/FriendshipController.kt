package no.ntnu.prog2007.ihostapi.controller

import com.google.cloud.firestore.Firestore
import jakarta.validation.Valid
import no.ntnu.prog2007.ihostapi.model.ErrorResponse
import no.ntnu.prog2007.ihostapi.model.FriendRequestRequest
import no.ntnu.prog2007.ihostapi.model.Friendship
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

@RestController
@RequestMapping("/api/friendships")
class FriendshipController(
    private val firestore: Firestore
) {
    private val logger = Logger.getLogger(FriendshipController::class.java.name)

    companion object {
        const val FRIENDSHIPS_COLLECTION = "friendships"
    }

    /**
     * Send a friend request to another user
     * Creates a new friendship with PENDING status
     */
    @PostMapping("/request")
    fun sendFriendRequest(@Valid @RequestBody request: FriendRequestRequest): ResponseEntity<Any> {
        return try {
            // Get current user from security context
            val currentUserId = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            val toUserId = request.toUserId

            // Prevent sending request to self
            if (currentUserId == toUserId) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse("INVALID_REQUEST", "Cannot send friend request to yourself"))
            }

            // Check if friendship already exists (in either direction)
            val existingFriendship = checkExistingFriendship(currentUserId, toUserId)
            if (existingFriendship != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse("FRIENDSHIP_EXISTS", "Friendship request already exists"))
            }

            // Create new friendship
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val timestamp = now.format(formatter)

            val friendshipData = mapOf(
                "user1Id" to currentUserId,
                "user2Id" to toUserId,
                "status" to "PENDING",
                "requestedBy" to currentUserId,
                "requestedAt" to timestamp,
                "respondedAt" to null
            )

            // Save to Firestore (without id field)
            val docRef = firestore.collection(FRIENDSHIPS_COLLECTION)
                .add(friendshipData)
                .get()

            val createdFriendship = Friendship(
                id = docRef.id,
                user1Id = currentUserId,
                user2Id = toUserId,
                status = "PENDING",
                requestedBy = currentUserId,
                requestedAt = timestamp,
                respondedAt = null
            )

            logger.info("Friend request sent from $currentUserId to $toUserId")
            ResponseEntity.status(HttpStatus.CREATED).body(createdFriendship)

        } catch (e: Exception) {
            logger.warning("Error sending friend request: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not send friend request"))
        }
    }

    /**
     * Accept a friend request
     * Updates friendship status to ACCEPTED
     */
    @PostMapping("/{friendshipId}/accept")
    fun acceptFriendRequest(@PathVariable friendshipId: String): ResponseEntity<Any> {
        return try {
            val currentUserId = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            // Get friendship document
            val friendshipDoc = firestore.collection(FRIENDSHIPS_COLLECTION)
                .document(friendshipId)
                .get()
                .get()

            if (!friendshipDoc.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "Friendship not found"))
            }

            val friendship = friendshipDoc.toObject(Friendship::class.java)?.copy(id = friendshipId)
                ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse("ERROR", "Could not parse friendship data"))

            // Verify current user is the recipient (user2)
            if (friendship.user2Id != currentUserId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse("FORBIDDEN", "You can only accept requests sent to you"))
            }

            // Verify friendship is pending
            if (friendship.status != "PENDING") {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse("INVALID_STATUS", "Friendship is not pending"))
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

            firestore.collection(FRIENDSHIPS_COLLECTION)
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
            ResponseEntity.ok(updatedFriendship)

        } catch (e: Exception) {
            logger.warning("Error accepting friend request: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not accept friend request"))
        }
    }

    /**
     * Decline a friend request
     * Updates friendship status to DECLINED
     */
    @PostMapping("/{friendshipId}/decline")
    fun declineFriendRequest(@PathVariable friendshipId: String): ResponseEntity<Any> {
        return try {
            val currentUserId = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            // Get friendship document
            val friendshipDoc = firestore.collection(FRIENDSHIPS_COLLECTION)
                .document(friendshipId)
                .get()
                .get()

            if (!friendshipDoc.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "Friendship not found"))
            }

            val friendship = friendshipDoc.toObject(Friendship::class.java)?.copy(id = friendshipId)
                ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse("ERROR", "Could not parse friendship data"))

            // Verify current user is the recipient (user2)
            if (friendship.user2Id != currentUserId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse("FORBIDDEN", "You can only decline requests sent to you"))
            }

            // Verify friendship is pending
            if (friendship.status != "PENDING") {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse("INVALID_STATUS", "Friendship is not pending"))
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

            firestore.collection(FRIENDSHIPS_COLLECTION)
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
            ResponseEntity.ok(updatedFriendship)

        } catch (e: Exception) {
            logger.warning("Error declining friend request: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not decline friend request"))
        }
    }

    /**
     * Remove a friend (delete friendship)
     * Can be done by either user in the friendship
     */
    @DeleteMapping("/{friendshipId}")
    fun removeFriend(@PathVariable friendshipId: String): ResponseEntity<Any> {
        return try {
            val currentUserId = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            // Get friendship document
            val friendshipDoc = firestore.collection(FRIENDSHIPS_COLLECTION)
                .document(friendshipId)
                .get()
                .get()

            if (!friendshipDoc.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "Friendship not found"))
            }

            val friendship = friendshipDoc.toObject(Friendship::class.java)?.copy(id = friendshipId)
                ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse("ERROR", "Could not parse friendship data"))

            // Verify current user is part of the friendship
            if (friendship.user1Id != currentUserId && friendship.user2Id != currentUserId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse("FORBIDDEN", "You can only remove your own friendships"))
            }

            // Delete the friendship
            firestore.collection(FRIENDSHIPS_COLLECTION)
                .document(friendshipId)
                .delete()
                .get()

            logger.info("Friendship removed: $friendshipId")
            ResponseEntity.ok(mapOf("message" to "Friendship removed successfully"))

        } catch (e: Exception) {
            logger.warning("Error removing friend: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not remove friend"))
        }
    }

    /**
     * Get pending friend requests (requests current user received)
     */
    @GetMapping("/pending")
    fun getPendingRequests(): ResponseEntity<Any> {
        return try {
            val currentUserId = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            // Query friendships where current user is user2 and status is PENDING
            val query = firestore.collection(FRIENDSHIPS_COLLECTION)
                .whereEqualTo("user2Id", currentUserId)
                .whereEqualTo("status", "PENDING")
                .get()
                .get()

            val friendships = query.documents.mapNotNull { doc ->
                doc.toObject(Friendship::class.java)?.copy(id = doc.id)
            }

            logger.info("Retrieved ${friendships.size} pending requests for user: $currentUserId")
            ResponseEntity.ok(friendships)

        } catch (e: Exception) {
            logger.warning("Error getting pending requests: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not retrieve pending requests"))
        }
    }

    /**
     * Get friends (accepted friendships)
     * Returns friendships where current user is either user1 or user2
     */
    @GetMapping("/friends")
    fun getFriends(): ResponseEntity<Any> {
        return try {
            val currentUserId = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            // Query friendships where current user is user1 and status is ACCEPTED
            val query1 = firestore.collection(FRIENDSHIPS_COLLECTION)
                .whereEqualTo("user1Id", currentUserId)
                .whereEqualTo("status", "ACCEPTED")
                .get()
                .get()

            val friendships1 = query1.documents.mapNotNull { doc ->
                doc.toObject(Friendship::class.java)?.copy(id = doc.id)
            }

            // Query friendships where current user is user2 and status is ACCEPTED
            val query2 = firestore.collection(FRIENDSHIPS_COLLECTION)
                .whereEqualTo("user2Id", currentUserId)
                .whereEqualTo("status", "ACCEPTED")
                .get()
                .get()

            val friendships2 = query2.documents.mapNotNull { doc ->
                doc.toObject(Friendship::class.java)?.copy(id = doc.id)
            }

            val allFriendships = friendships1 + friendships2

            logger.info("Retrieved ${allFriendships.size} friends for user: $currentUserId")
            ResponseEntity.ok(allFriendships)

        } catch (e: Exception) {
            logger.warning("Error getting friends: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not retrieve friends"))
        }
    }

    /**
     * Get sent friend requests (requests current user sent that are still pending)
     */
    @GetMapping("/sent")
    fun getSentRequests(): ResponseEntity<Any> {
        return try {
            val currentUserId = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            // Query friendships where current user is user1 and status is PENDING
            val query = firestore.collection(FRIENDSHIPS_COLLECTION)
                .whereEqualTo("user1Id", currentUserId)
                .whereEqualTo("status", "PENDING")
                .get()
                .get()

            val friendships = query.documents.mapNotNull { doc ->
                doc.toObject(Friendship::class.java)?.copy(id = doc.id)
            }

            logger.info("Retrieved ${friendships.size} sent requests for user: $currentUserId")
            ResponseEntity.ok(friendships)

        } catch (e: Exception) {
            logger.warning("Error getting sent requests: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not retrieve sent requests"))
        }
    }

    /**
     * Helper function to check if a friendship already exists between two users
     * Checks both directions (user1->user2 and user2->user1)
     */
    private fun checkExistingFriendship(userId1: String, userId2: String): Friendship? {
        return try {
            // Check user1 -> user2
            val query1 = firestore.collection(FRIENDSHIPS_COLLECTION)
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
            val query2 = firestore.collection(FRIENDSHIPS_COLLECTION)
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
