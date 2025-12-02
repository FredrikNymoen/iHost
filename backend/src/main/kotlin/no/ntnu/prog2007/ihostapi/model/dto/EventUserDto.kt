package no.ntnu.prog2007.ihostapi.model.dto

import no.ntnu.prog2007.ihostapi.model.entity.EventUser
import no.ntnu.prog2007.ihostapi.model.entity.EventUserRole
import no.ntnu.prog2007.ihostapi.model.entity.EventUserStatus

/**
 * Response DTO for EventUser with Firestore document ID
 */
data class EventUserResponse(
    val id: String,  // Firestore document ID
    val eventId: String,
    val userId: String,
    val status: EventUserStatus,
    val role: EventUserRole,
    val invitedAt: String,
    val respondedAt: String?
) {
    companion object {
        fun from(eventUser: EventUser, documentId: String): EventUserResponse {
            return EventUserResponse(
                id = documentId,
                eventId = eventUser.eventId,
                userId = eventUser.userId,
                status = eventUser.status,
                role = eventUser.role,
                invitedAt = eventUser.invitedAt,
                respondedAt = eventUser.respondedAt
            )
        }
    }
}
