package no.ntnu.prog2007.ihostapi.service

import no.ntnu.prog2007.ihostapi.model.dto.EventUserResponse
import no.ntnu.prog2007.ihostapi.model.entity.EventUser

/**
 * Service interface for EventUser operations
 */
interface EventUserService {
    fun inviteUsers(eventId: String, userIds: List<String>, creatorId: String): List<EventUserResponse>
    fun acceptInvitation(eventUserId: String, userId: String): String
    fun declineInvitation(eventUserId: String, userId: String): String
    fun getEventAttendees(eventId: String, status: String?): List<EventUserResponse>
    fun getMyEvents(userId: String, status: String?): List<Map<String, Any?>>
}
