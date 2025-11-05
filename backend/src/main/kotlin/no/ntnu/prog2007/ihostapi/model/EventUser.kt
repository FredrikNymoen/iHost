package no.ntnu.prog2007.ihostapi.model

import java.util.UUID

/**
 * Represents a user's relationship to an event
 */
data class EventUser(
    val id: String = UUID.randomUUID().toString(),
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
