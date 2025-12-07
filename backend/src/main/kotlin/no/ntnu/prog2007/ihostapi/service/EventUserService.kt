package no.ntnu.prog2007.ihostapi.service

import no.ntnu.prog2007.ihostapi.model.dto.EventUserResponse
import no.ntnu.prog2007.ihostapi.model.entity.EventUser

/**
 * Service interface for EventUser operations
 */
interface EventUserService {
    /**
     * Invite users to an event
     * @param eventId The ID of the event
     * @param userIds List of user IDs to invite
     * @param creatorId The ID of the event creator making the invitations
     * @return List of EventUserResponse objects for the invited users
     * @throws IllegalArgumentException if the creator is not the event owner
     */
    fun inviteUsers(eventId: String, userIds: List<String>, creatorId: String): List<EventUserResponse>

    /**
     * Accept an event invitation
     * @param eventUserId The ID of the event-user relationship
     * @param userId The ID of the user accepting the invitation
     * @return The event ID of the accepted invitation
     * @throws IllegalArgumentException if user is not the invitation recipient
     */
    fun acceptInvitation(eventUserId: String, userId: String): String

    /**
     * Decline an event invitation
     * @param eventUserId The ID of the event-user relationship
     * @param userId The ID of the user declining the invitation
     * @return The event ID of the declined invitation
     * @throws IllegalArgumentException if user is not the invitation recipient
     */
    fun declineInvitation(eventUserId: String, userId: String): String

    /**
     * Get all attendees for an event
     * @param eventId The ID of the event
     * @param status Optional status filter (PENDING, ACCEPTED, DECLINED, CREATOR)
     * @return List of EventUserResponse objects for the event attendees
     */
    fun getEventAttendees(eventId: String, status: String?): List<EventUserResponse>

    /**
     * Get all events for a user
     * @param userId The ID of the user
     * @param status Optional status filter (PENDING, ACCEPTED, DECLINED, CREATOR)
     * @return List of event data maps containing event details and user's role/status
     */
    fun getMyEvents(userId: String, status: String?): List<Map<String, Any?>>
}
