package no.ntnu.prog2007.ihost.data.model.domain

/**
 * Data class representing a friendship between two users
 * Stored in a top-level "friendships" collection in Firestore
 * Timestamps are stored as ISO-8601 strings to match backend format
 */
data class Friendship(
    val id: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val status: FriendshipStatus = FriendshipStatus.PENDING,
    val requestedBy: String = "",
    val requestedAt: String? = null,
    val respondedAt: String? = null
)

enum class FriendshipStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

/**
 * Helper extension to get the other user's ID in a friendship
 */
fun Friendship.getOtherUserId(currentUserId: String): String {
    return if (user1Id == currentUserId) user2Id else user1Id
}

/**
 * Helper extension to check if current user is the requester
 */
fun Friendship.isRequestedByMe(currentUserId: String): Boolean {
    return requestedBy == currentUserId
}
