package no.ntnu.prog2007.ihostapi.controller

import no.ntnu.prog2007.ihostapi.exception.UnauthorizedException
import no.ntnu.prog2007.ihostapi.model.dto.*
import no.ntnu.prog2007.ihostapi.service.EventUserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

/**
 * REST controller for managing event-user relationships and participation.
 *
 * This controller handles the many-to-many relationship between users and events
 * through the EventUser junction entity. It manages:
 * - Event invitations (sent by event creators or participants)
 * - Invitation acceptance/decline workflow
 * - Retrieving event attendees and user's events
 *
 * EventUser relationships track both the status (INVITED, ACCEPTED, DECLINED, CREATOR)
 * and the role (CREATOR, PARTICIPANT) of each user in an event.
 *
 * @property eventUserService Business logic service for event-user operations
 * @see no.ntnu.prog2007.ihostapi.service.EventUserService for business logic
 * @see no.ntnu.prog2007.ihostapi.model.entity.EventUser for relationship model
 */
@RestController
@RequestMapping("/api/event-users")
class EventUserController(
    private val eventUserService: EventUserService
) {
    private val logger = Logger.getLogger(EventUserController::class.java.name)

    /**
     * Sends event invitations to multiple users.
     *
     * Only event creators or existing participants can invite others.
     * Creates EventUser records with INVITED status for each user.
     * Duplicate invitations are handled gracefully by the service.
     *
     * @param request Contains eventId and list of user IDs to invite
     * @return Success message with count of invited users
     * @throws ForbiddenException if the current user cannot invite to this event
     */
    @PostMapping("/invite")
    fun inviteUsers(@RequestBody request: InviteUsersRequest): ResponseEntity<Map<String, Any>> {
        val currentUserId = getCurrentUserId()
        val invitedUsers = eventUserService.inviteUsers(request.eventId, request.userIds, currentUserId)

        logger.info("Invited ${invitedUsers.size} users to event ${request.eventId}")

        return ResponseEntity.ok(mapOf(
            "message" to "Users invited successfully",
            "invitedCount" to invitedUsers.size,
            "invitedUsers" to invitedUsers
        ))
    }

    /**
     * Accepts an event invitation.
     *
     * Changes the user's status from INVITED to ACCEPTED. Only the invited
     * user can accept their own invitation. For paid events, payment must
     * be processed before calling this endpoint.
     *
     * @param eventUserId The EventUser relationship ID (not the event ID)
     * @return Success message with the event ID
     * @throws ForbiddenException if user is not the invitation recipient
     */
    @PostMapping("/{eventUserId}/accept")
    fun acceptInvitation(@PathVariable eventUserId: String): ResponseEntity<Map<String, String>> {
        val currentUserId = getCurrentUserId()
        val eventId = eventUserService.acceptInvitation(eventUserId, currentUserId)

        logger.info("User $currentUserId accepted invitation to event $eventId")

        return ResponseEntity.ok(mapOf(
            "message" to "Invitation accepted",
            "eventId" to eventId
        ))
    }

    /**
     * Declines an event invitation.
     *
     * Changes the user's status from INVITED to DECLINED. The relationship
     * record is kept for audit purposes rather than deleted. Only the invited
     * user can decline their own invitation.
     *
     * @param eventUserId The EventUser relationship ID (not the event ID)
     * @return Success message with the event ID
     * @throws ForbiddenException if user is not the invitation recipient
     */
    @PostMapping("/{eventUserId}/decline")
    fun declineInvitation(@PathVariable eventUserId: String): ResponseEntity<Map<String, String>> {
        val currentUserId = getCurrentUserId()
        val eventId = eventUserService.declineInvitation(eventUserId, currentUserId)

        logger.info("User $currentUserId declined invitation to event $eventId")

        return ResponseEntity.ok(mapOf(
            "message" to "Invitation declined",
            "eventId" to eventId
        ))
    }

    /**
     * Retrieves all attendees for a specific event.
     *
     * Returns user information and relationship details for everyone associated
     * with the event. Optionally filters by status (INVITED, ACCEPTED, DECLINED, CREATOR).
     *
     * @param eventId The event to retrieve attendees for
     * @param status Optional status filter (e.g., "ACCEPTED" for confirmed attendees only)
     * @return List of attendees with their user info and event relationship details
     */
    @GetMapping("/event/{eventId}")
    fun getEventAttendees(
        @PathVariable eventId: String,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<List<EventUserResponse>> {
        getCurrentUserId() // Verify authentication
        val eventUsers = eventUserService.getEventAttendees(eventId, status)

        return ResponseEntity.ok(eventUsers)
    }

    /**
     * Retrieves all events the authenticated user is associated with.
     *
     * Returns events where the user has any relationship (INVITED, ACCEPTED, CREATOR).
     * Optionally filters by status to show only specific event types.
     * Each event includes the full event data plus user's status and role.
     *
     * @param status Optional status filter (e.g., "INVITED" for pending invitations only)
     * @return List of events with user relationship details
     */
    @GetMapping("/my-events")
    fun getMyEvents(@RequestParam(required = false) status: String?): ResponseEntity<List<Map<String, Any?>>> {
        val currentUserId = getCurrentUserId()
        val events = eventUserService.getMyEvents(currentUserId, status)

        return ResponseEntity.ok(events)
    }

    /**
     * Extracts the Firebase UID from the SecurityContext.
     *
     * The UID is placed in the SecurityContext by [FirebaseTokenFilter]
     * after successfully validating the JWT token.
     *
     * @return Firebase UID of the authenticated user
     * @throws UnauthorizedException if no valid authentication exists
     * @see no.ntnu.prog2007.ihostapi.security.filter.FirebaseTokenFilter
     */
    private fun getCurrentUserId(): String {
        return SecurityContextHolder.getContext().authentication.principal as? String
            ?: throw UnauthorizedException("Token is invalid or missing")
    }
}

/**
 * Request payload for inviting users to an event.
 *
 * @property eventId The Firestore document ID of the event
 * @property userIds List of Firebase UIDs of users to invite
 */
data class InviteUsersRequest(
    val eventId: String,
    val userIds: List<String>
)
