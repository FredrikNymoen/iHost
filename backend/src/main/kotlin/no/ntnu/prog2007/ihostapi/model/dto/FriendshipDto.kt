package no.ntnu.prog2007.ihostapi.model.dto

import jakarta.validation.constraints.NotBlank

/**
 * Request model for sending a friend request
 */
data class FriendRequestRequest(
    @field:NotBlank(message = "To user ID is required")
    val toUserId: String = ""
)
