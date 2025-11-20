package no.ntnu.prog2007.ihostapi.model

import jakarta.validation.constraints.NotBlank

/**
 * Friendship model representing a friendship/friend request between two users
 * Stored in Firestore "friendships" collection
 */
data class Friendship(
    @field:NotBlank(message = "ID is required")
    val id: String = "",

    @field:NotBlank(message = "User1 ID is required")
    val user1Id: String = "",

    @field:NotBlank(message = "User2 ID is required")
    val user2Id: String = "",

    @field:NotBlank(message = "Status is required")
    val status: String = "PENDING", // PENDING, ACCEPTED, DECLINED

    @field:NotBlank(message = "Requested by is required")
    val requestedBy: String = "",

    val requestedAt: String? = null,

    val respondedAt: String? = null
)

/**
 * Request model for sending a friend request
 */
data class FriendRequestRequest(
    @field:NotBlank(message = "To user ID is required")
    val toUserId: String = ""
)
