package no.ntnu.prog2007.ihostapi.model.entity

/**
 * Represents a user's relationship to an event
 * Note: The document ID in Firestore is used as the identifier, not stored in this model
 */
data class EventUser(
    val eventId: String = "",
    val userId: String = "",
    val status: EventUserStatus = EventUserStatus.PENDING,
    val role: EventUserRole = EventUserRole.ATTENDEE,
    val invitedAt: String = "",
    val respondedAt: String? = null
)

/**
 * Status of a user's relationship to an event
 */
enum class EventUserStatus {
    PENDING,    // Invited but not yet responded
    ACCEPTED,   // Accepted invitation
    DECLINED,   // Declined invitation
    CREATOR     // Created the event
}

/**
 * Role of a user in an event
 */
enum class EventUserRole {
    CREATOR,    // Event creator/host
    ATTENDEE    // Regular attendee
}
