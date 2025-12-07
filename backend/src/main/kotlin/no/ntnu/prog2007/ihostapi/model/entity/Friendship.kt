package no.ntnu.prog2007.ihostapi.model.entity

import jakarta.validation.constraints.NotBlank

/**
 * Friendship entity representing a connection or pending request between two users.
 *
 * Models the social graph of the application, enabling users to build a friends list
 * for easier event invitations. A friendship goes through a request/accept workflow
 * to prevent unwanted connections.
 *
 * **Storage**: Firestore `friendships` collection. The document ID is also stored
 * in the [id] field for convenience when returning data to clients.
 *
 * **Bidirectional relationship**: A single Friendship document represents the
 * connection between two users. Queries must check both `user1Id` and `user2Id`
 * to find all friendships for a given user.
 *
 * **Status workflow**:
 * 1. User A sends request → status = PENDING, requestedBy = A
 * 2. User B accepts → status = ACCEPTED, respondedAt = timestamp
 * 3. Alternative: User B declines → status = DECLINED
 *
 * @property id Firestore document ID (stored for client convenience)
 * @property user1Id Firebase UID of one user in the friendship
 * @property user2Id Firebase UID of the other user
 * @property status Current state: PENDING, ACCEPTED, or DECLINED
 * @property requestedBy UID of the user who initiated the friend request
 * @property requestedAt ISO-8601 timestamp when the request was sent
 * @property respondedAt ISO-8601 timestamp when the request was accepted/declined
 *
 * @see no.ntnu.prog2007.ihostapi.service.impl.FriendshipServiceImpl for status transitions
 */
data class Friendship(
    @field:NotBlank(message = "ID is required")
    val id: String = "",

    @field:NotBlank(message = "User1 ID is required")
    val user1Id: String = "",

    @field:NotBlank(message = "User2 ID is required")
    val user2Id: String = "",

    @field:NotBlank(message = "Status is required")
    val status: String = "PENDING",

    @field:NotBlank(message = "Requested by is required")
    val requestedBy: String = "",

    val requestedAt: String? = null,

    val respondedAt: String? = null
)
