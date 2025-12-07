package no.ntnu.prog2007.ihost.data.model.domain

/**
 * Represents a user's relationship to an event
 */
data class EventUser(
    val id: String,
    val eventId: String,
    val userId: String,
    val status: String, // PENDING, ACCEPTED, DECLINED, CREATOR
    val role: String, // CREATOR, ATTENDEE
    val invitedAt: String,
    val respondedAt: String?
)
