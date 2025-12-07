package no.ntnu.prog2007.ihostapi.model.dto

import jakarta.validation.constraints.NotBlank

/**
 * Request payload for initiating a friend request.
 *
 * The sender's UID is extracted from the authenticated security context,
 * so only the recipient needs to be specified. This prevents users from
 * sending requests on behalf of others.
 *
 * The service layer validates that:
 * - Users cannot send requests to themselves
 * - No existing friendship/request exists between the users
 *
 * @property toUserId Firebase UID of the user to send the request to
 */
data class FriendRequestRequest(
    @field:NotBlank(message = "To user ID is required")
    val toUserId: String = ""
)
