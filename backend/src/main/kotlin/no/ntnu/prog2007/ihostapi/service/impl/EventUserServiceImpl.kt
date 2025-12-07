package no.ntnu.prog2007.ihostapi.service.impl

import no.ntnu.prog2007.ihostapi.exception.ResourceNotFoundException
import no.ntnu.prog2007.ihostapi.model.dto.EventUserResponse
import no.ntnu.prog2007.ihostapi.model.entity.EventUser
import no.ntnu.prog2007.ihostapi.model.entity.EventUserRole
import no.ntnu.prog2007.ihostapi.model.entity.EventUserStatus
import no.ntnu.prog2007.ihostapi.repository.EventRepository
import no.ntnu.prog2007.ihostapi.repository.EventUserRepository
import no.ntnu.prog2007.ihostapi.service.EventUserService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

/**
 * Service implementation for EventUser operations
 */
@Service
class EventUserServiceImpl(
    private val eventUserRepository: EventUserRepository,
    private val eventRepository: EventRepository
) : EventUserService {
    private val logger = Logger.getLogger(EventUserServiceImpl::class.java.name)

    /**
     * Invite users to an event
     * @param eventId The ID of the event
     * @param userIds List of user IDs to invite
     * @param creatorId The ID of the event creator making the invitations
     * @return List of EventUserResponse objects for the invited users
     * @throws IllegalArgumentException if the creator is not the event owner
     */
    override fun inviteUsers(eventId: String, userIds: List<String>, creatorId: String): List<EventUserResponse> {
        // Verify event exists and user is the creator
        val event = eventRepository.findById(eventId)
            ?: throw ResourceNotFoundException("Event not found")

        if (event.creatorUid != creatorId) {
            throw IllegalArgumentException("Only the event creator can invite users")
        }

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val invitedUsers = mutableListOf<EventUserResponse>()

        // Create event_user documents for each invited user
        for (userId in userIds) {
            // Check if user is already invited
            val existingInvite = eventUserRepository.findByEventIdAndUserId(eventId, userId)

            if (existingInvite != null) {
                logger.info("User $userId already invited to event $eventId")
                continue
            }

            val eventUser = EventUser(
                eventId = eventId,
                userId = userId,
                status = EventUserStatus.PENDING,
                role = EventUserRole.ATTENDEE,
                invitedAt = now,
                respondedAt = null
            )

            val (docId, savedEventUser) = eventUserRepository.save(eventUser)
            invitedUsers.add(EventUserResponse.from(savedEventUser, docId))
        }

        logger.info("Invited ${invitedUsers.size} users to event $eventId")
        return invitedUsers
    }

    /**
     * Accept an event invitation
     * @param eventUserId The ID of the event-user relationship
     * @param userId The ID of the user accepting the invitation
     * @return The event ID of the accepted invitation
     * @throws IllegalArgumentException if user is not the invitation recipient
     */
    override fun acceptInvitation(eventUserId: String, userId: String): String {
        val eventUser = eventUserRepository.findById(eventUserId)
            ?: throw ResourceNotFoundException("Invitation not found")

        if (eventUser.userId != userId) {
            throw IllegalArgumentException("You can only respond to your own invitations")
        }

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        eventUserRepository.update(
            eventUserId,
            mapOf(
                "status" to EventUserStatus.ACCEPTED.name,
                "respondedAt" to now
            )
        )

        logger.info("User $userId accepted invitation to event ${eventUser.eventId}")
        return eventUser.eventId
    }

    /**
     * Decline an event invitation
     * @param eventUserId The ID of the event-user relationship
     * @param userId The ID of the user declining the invitation
     * @return The event ID of the declined invitation
     * @throws IllegalArgumentException if user is not the invitation recipient
     */
    override fun declineInvitation(eventUserId: String, userId: String): String {
        val eventUser = eventUserRepository.findById(eventUserId)
            ?: throw ResourceNotFoundException("Invitation not found")

        if (eventUser.userId != userId) {
            throw IllegalArgumentException("You can only respond to your own invitations")
        }

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        eventUserRepository.update(
            eventUserId,
            mapOf(
                "status" to EventUserStatus.DECLINED.name,
                "respondedAt" to now
            )
        )

        logger.info("User $userId declined invitation to event ${eventUser.eventId}")
        return eventUser.eventId
    }

    /**
     * Get all attendees for an event
     * @param eventId The ID of the event
     * @param status Optional status filter (PENDING, ACCEPTED, DECLINED, CREATOR)
     * @return List of EventUserResponse objects for the event attendees
     */
    override fun getEventAttendees(eventId: String, status: String?): List<EventUserResponse> {
        val eventUsers = if (status != null) {
            eventUserRepository.findByEventIdAndStatus(eventId, status).map { (docId, eventUser) ->
                EventUserResponse.from(eventUser, docId)
            }
        } else {
            eventUserRepository.findByEventId(eventId).map { (docId, eventUser) ->
                EventUserResponse.from(eventUser, docId)
            }
        }

        return eventUsers
    }

    /**
     * Get all events for a user
     * @param userId The ID of the user
     * @param status Optional status filter (PENDING, ACCEPTED, DECLINED, CREATOR)
     * @return List of event data maps containing event details and user's role/status
     */
    override fun getMyEvents(userId: String, status: String?): List<Map<String, Any?>> {
        val eventUsers = if (status != null) {
            eventUserRepository.findByUserIdAndStatus(userId, status).map { (_, eventUser) -> eventUser }
        } else {
            eventUserRepository.findByUserId(userId).map { (_, eventUser) -> eventUser }
        }

        // Fetch the actual event details for each
        val events = eventUsers.mapNotNull { eventUser ->
            try {
                val event = eventRepository.findById(eventUser.eventId)
                event?.let {
                    mapOf(
                        "id" to eventUser.eventId,
                        "event" to event,
                        "userStatus" to eventUser.status,
                        "userRole" to eventUser.role
                    )
                }
            } catch (e: Exception) {
                logger.warning("Failed to fetch event ${eventUser.eventId}: ${e.message}")
                null
            }
        }

        return events
    }
}