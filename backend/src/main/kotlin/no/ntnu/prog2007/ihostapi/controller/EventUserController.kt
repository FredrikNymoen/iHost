package no.ntnu.prog2007.ihostapi.controller

import no.ntnu.prog2007.ihostapi.exception.UnauthorizedException
import no.ntnu.prog2007.ihostapi.model.dto.*
import no.ntnu.prog2007.ihostapi.service.EventUserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

/**
 * Controller for managing event-user relationships
 */
@RestController
@RequestMapping("/api/event-users")
class EventUserController(
    private val eventUserService: EventUserService
) {
    private val logger = Logger.getLogger(EventUserController::class.java.name)

    /**
     * Invite users to an event
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
     * Accept an event invitation
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
     * Decline an event invitation
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
     * Get all attendees for an event
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
     * Get all events for current user
     */
    @GetMapping("/my-events")
    fun getMyEvents(@RequestParam(required = false) status: String?): ResponseEntity<List<Map<String, Any?>>> {
        val currentUserId = getCurrentUserId()
        val events = eventUserService.getMyEvents(currentUserId, status)

        return ResponseEntity.ok(events)
    }

    /**
     * Helper function to get current authenticated user ID
     */
    private fun getCurrentUserId(): String {
        return SecurityContextHolder.getContext().authentication.principal as? String
            ?: throw UnauthorizedException("Token is invalid or missing")
    }
}

/**
 * Request to invite users to an event
 */
data class InviteUsersRequest(
    val eventId: String,
    val userIds: List<String>
)
