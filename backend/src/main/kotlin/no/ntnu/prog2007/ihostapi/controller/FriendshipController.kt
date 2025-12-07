package no.ntnu.prog2007.ihostapi.controller

import jakarta.validation.Valid
import no.ntnu.prog2007.ihostapi.exception.UnauthorizedException
import no.ntnu.prog2007.ihostapi.model.dto.FriendRequestRequest
import no.ntnu.prog2007.ihostapi.model.entity.Friendship
import no.ntnu.prog2007.ihostapi.service.FriendshipService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

/**
 * REST controller for managing user friendships and friend requests.
 *
 * Implements a bidirectional friendship system where:
 * - Users send friend requests to other users
 * - Recipients can accept or decline requests
 * - Accepted friendships are symmetric (both users are friends)
 * - Users can remove friendships
 *
 * Friendship status values: PENDING, ACCEPTED, DECLINED
 * The system tracks who initiated the request (fromUserId) and the recipient (toUserId).
 *
 * @property friendshipService Business logic service for friendship operations
 * @see no.ntnu.prog2007.ihostapi.service.FriendshipService for business logic
 * @see no.ntnu.prog2007.ihostapi.model.entity.Friendship for friendship model
 */
@RestController
@RequestMapping("/api/friendships")
class FriendshipController(
    private val friendshipService: FriendshipService
) {
    private val logger = Logger.getLogger(FriendshipController::class.java.name)

    /**
     * Sends a friend request to another user.
     *
     * Creates a friendship record with PENDING status. Users cannot send
     * duplicate requests or friend themselves. Returns the created friendship.
     *
     * @param request Contains the target user's ID (toUserId)
     * @return The created friendship with PENDING status (HTTP 201)
     * @throws IllegalArgumentException if request is invalid (duplicate, self-friending)
     */
    @PostMapping("/request")
    fun sendFriendRequest(@Valid @RequestBody request: FriendRequestRequest): ResponseEntity<Friendship> {
        val currentUserId = getCurrentUserId()
        val friendship = friendshipService.sendFriendRequest(currentUserId, request.toUserId)

        logger.info("Friend request sent from $currentUserId to ${request.toUserId}")
        return ResponseEntity.status(HttpStatus.CREATED).body(friendship)
    }

    /**
     * Accepts a pending friend request.
     *
     * Changes the friendship status from PENDING to ACCEPTED. Only the request
     * recipient (toUserId) can accept the request. After acceptance, both users
     * see each other as friends.
     *
     * @param friendshipId The Firestore document ID of the friendship
     * @return The updated friendship with ACCEPTED status
     * @throws ForbiddenException if current user is not the request recipient
     */
    @PostMapping("/{friendshipId}/accept")
    fun acceptFriendRequest(@PathVariable friendshipId: String): ResponseEntity<Friendship> {
        val currentUserId = getCurrentUserId()
        val friendship = friendshipService.acceptFriendRequest(friendshipId, currentUserId)

        logger.info("Friend request accepted: $friendshipId")
        return ResponseEntity.ok(friendship)
    }

    /**
     * Declines a pending friend request.
     *
     * Changes the friendship status from PENDING to DECLINED. Only the request
     * recipient (toUserId) can decline. The record is kept for audit purposes.
     *
     * @param friendshipId The Firestore document ID of the friendship
     * @return The updated friendship with DECLINED status
     * @throws ForbiddenException if current user is not the request recipient
     */
    @PostMapping("/{friendshipId}/decline")
    fun declineFriendRequest(@PathVariable friendshipId: String): ResponseEntity<Friendship> {
        val currentUserId = getCurrentUserId()
        val friendship = friendshipService.declineFriendRequest(friendshipId, currentUserId)

        logger.info("Friend request declined: $friendshipId")
        return ResponseEntity.ok(friendship)
    }

    /**
     * Removes a friendship.
     *
     * Permanently deletes the friendship record from Firestore. Either user
     * in an accepted friendship can remove it. Users can also delete their
     * own sent pending requests.
     *
     * @param friendshipId The Firestore document ID of the friendship
     * @return Success message
     * @throws ForbiddenException if user is not part of this friendship
     */
    @DeleteMapping("/{friendshipId}")
    fun removeFriend(@PathVariable friendshipId: String): ResponseEntity<Map<String, String>> {
        val currentUserId = getCurrentUserId()
        friendshipService.removeFriend(friendshipId, currentUserId)

        logger.info("Friendship removed: $friendshipId")
        return ResponseEntity.ok(mapOf("message" to "Friendship removed successfully"))
    }

    /**
     * Retrieves pending friend requests received by the current user.
     *
     * Returns all friendships where:
     * - Current user is the recipient (toUserId)
     * - Status is PENDING
     *
     * These are requests waiting for the user's response (accept/decline).
     *
     * @return List of pending incoming friend requests
     */
    @GetMapping("/pending")
    fun getPendingRequests(): ResponseEntity<List<Friendship>> {
        val currentUserId = getCurrentUserId()
        val friendships = friendshipService.getPendingRequests(currentUserId)

        logger.info("Retrieved ${friendships.size} pending requests for user: $currentUserId")
        return ResponseEntity.ok(friendships)
    }

    /**
     * Retrieves all accepted friendships for the current user.
     *
     * Returns all friendships where:
     * - Current user is either fromUserId or toUserId
     * - Status is ACCEPTED
     *
     * This is the user's friends list.
     *
     * @return List of accepted friendships
     */
    @GetMapping("/friends")
    fun getFriends(): ResponseEntity<List<Friendship>> {
        val currentUserId = getCurrentUserId()
        val allFriendships = friendshipService.getFriends(currentUserId)

        logger.info("Retrieved ${allFriendships.size} friends for user: $currentUserId")
        return ResponseEntity.ok(allFriendships)
    }

    /**
     * Retrieves pending friend requests sent by the current user.
     *
     * Returns all friendships where:
     * - Current user is the sender (fromUserId)
     * - Status is PENDING
     *
     * These are outgoing requests awaiting response from other users.
     *
     * @return List of pending outgoing friend requests
     */
    @GetMapping("/sent")
    fun getSentRequests(): ResponseEntity<List<Friendship>> {
        val currentUserId = getCurrentUserId()
        val friendships = friendshipService.getSentRequests(currentUserId)

        logger.info("Retrieved ${friendships.size} sent requests for user: $currentUserId")
        return ResponseEntity.ok(friendships)
    }

    /**
     * Extracts the Firebase UID from the SecurityContext.
     *
     * The UID is placed in the SecurityContext by [FirebaseTokenFilter]
     * after successfully validating the JWT token.
     *
     * @return Firebase UID of the authenticated user
     * @throws UnauthorizedException if no valid authentication exists
     * @see no.ntnu.prog2007.ihostapi.security.filter.FirebaseTokenFilter
     */
    private fun getCurrentUserId(): String {
        return SecurityContextHolder.getContext().authentication.principal as? String
            ?: throw UnauthorizedException("Token is invalid or missing")
    }
}
