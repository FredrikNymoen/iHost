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

@RestController
@RequestMapping("/api/friendships")
class FriendshipController(
    private val friendshipService: FriendshipService
) {
    private val logger = Logger.getLogger(FriendshipController::class.java.name)

    /**
     * Send a friend request to another user
     */
    @PostMapping("/request")
    fun sendFriendRequest(@Valid @RequestBody request: FriendRequestRequest): ResponseEntity<Friendship> {
        val currentUserId = getCurrentUserId()
        val friendship = friendshipService.sendFriendRequest(currentUserId, request.toUserId)

        logger.info("Friend request sent from $currentUserId to ${request.toUserId}")
        return ResponseEntity.status(HttpStatus.CREATED).body(friendship)
    }

    /**
     * Accept a friend request
     */
    @PostMapping("/{friendshipId}/accept")
    fun acceptFriendRequest(@PathVariable friendshipId: String): ResponseEntity<Friendship> {
        val currentUserId = getCurrentUserId()
        val friendship = friendshipService.acceptFriendRequest(friendshipId, currentUserId)

        logger.info("Friend request accepted: $friendshipId")
        return ResponseEntity.ok(friendship)
    }

    /**
     * Decline a friend request
     */
    @PostMapping("/{friendshipId}/decline")
    fun declineFriendRequest(@PathVariable friendshipId: String): ResponseEntity<Friendship> {
        val currentUserId = getCurrentUserId()
        val friendship = friendshipService.declineFriendRequest(friendshipId, currentUserId)

        logger.info("Friend request declined: $friendshipId")
        return ResponseEntity.ok(friendship)
    }

    /**
     * Remove a friend (delete friendship)
     */
    @DeleteMapping("/{friendshipId}")
    fun removeFriend(@PathVariable friendshipId: String): ResponseEntity<Map<String, String>> {
        val currentUserId = getCurrentUserId()
        friendshipService.removeFriend(friendshipId, currentUserId)

        logger.info("Friendship removed: $friendshipId")
        return ResponseEntity.ok(mapOf("message" to "Friendship removed successfully"))
    }

    /**
     * Get pending friend requests (requests current user received)
     */
    @GetMapping("/pending")
    fun getPendingRequests(): ResponseEntity<List<Friendship>> {
        val currentUserId = getCurrentUserId()
        val friendships = friendshipService.getPendingRequests(currentUserId)

        logger.info("Retrieved ${friendships.size} pending requests for user: $currentUserId")
        return ResponseEntity.ok(friendships)
    }

    /**
     * Get friends (accepted friendships)
     */
    @GetMapping("/friends")
    fun getFriends(): ResponseEntity<List<Friendship>> {
        val currentUserId = getCurrentUserId()
        val allFriendships = friendshipService.getFriends(currentUserId)

        logger.info("Retrieved ${allFriendships.size} friends for user: $currentUserId")
        return ResponseEntity.ok(allFriendships)
    }

    /**
     * Get sent friend requests (requests current user sent that are still pending)
     */
    @GetMapping("/sent")
    fun getSentRequests(): ResponseEntity<List<Friendship>> {
        val currentUserId = getCurrentUserId()
        val friendships = friendshipService.getSentRequests(currentUserId)

        logger.info("Retrieved ${friendships.size} sent requests for user: $currentUserId")
        return ResponseEntity.ok(friendships)
    }

    /**
     * Helper function to get current authenticated user ID
     */
    private fun getCurrentUserId(): String {
        return SecurityContextHolder.getContext().authentication.principal as? String
            ?: throw UnauthorizedException("Token is invalid or missing")
    }
}
