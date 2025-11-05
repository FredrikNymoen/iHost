package no.ntnu.prog2007.ihostapi.controller

import com.google.cloud.firestore.Firestore
import no.ntnu.prog2007.ihostapi.model.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

/**
 * Controller for managing event-user relationships
 * Handles invitations, acceptances, and attendee management
 */
@RestController
@RequestMapping("/api/event-users")
class EventUserController(
    private val firestore: Firestore
) {
    private val logger = Logger.getLogger(EventUserController::class.java.name)

    companion object {
        const val EVENT_USERS_COLLECTION = "event_users"
        const val EVENTS_COLLECTION = "events"
    }

    /**
     * Invite users to an event
     * Only the event creator can invite users
     */
    @PostMapping("/invite")
    fun inviteUsers(
        @RequestBody request: InviteUsersRequest
    ): ResponseEntity<Any> {
        return try {
            val currentUserId = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            // Verify event exists and user is the creator
            val eventDoc = firestore.collection(EVENTS_COLLECTION)
                .document(request.eventId)
                .get()
                .get()

            if (!eventDoc.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "Event not found"))
            }

            val event = eventDoc.toObject(Event::class.java)
            if (event?.creatorUid != currentUserId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse("FORBIDDEN", "Only the event creator can invite users"))
            }

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val invitedUsers = mutableListOf<EventUser>()

            // Create event_user documents for each invited user
            for (userId in request.userIds) {
                // Check if user is already invited
                val existingInvite = firestore.collection(EVENT_USERS_COLLECTION)
                    .whereEqualTo("eventId", request.eventId)
                    .whereEqualTo("userId", userId)
                    .get()
                    .get()

                if (!existingInvite.isEmpty) {
                    logger.info("User $userId already invited to event ${request.eventId}")
                    continue
                }

                val eventUser = EventUser(
                    eventId = request.eventId,
                    userId = userId,
                    status = EventUserStatus.PENDING,
                    role = EventUserRole.ATTENDEE,
                    invitedAt = now,
                    respondedAt = null
                )

                val docRef = firestore.collection(EVENT_USERS_COLLECTION).document()
                docRef.set(eventUser).get()
                // Add eventUser with Firestore document ID
                invitedUsers.add(eventUser.copy(id = docRef.id))
            }

            logger.info("Invited ${invitedUsers.size} users to event ${request.eventId}")

            ResponseEntity.ok(mapOf(
                "message" to "Users invited successfully",
                "invitedCount" to invitedUsers.size,
                "invitedUsers" to invitedUsers
            ))
        } catch (e: Exception) {
            logger.severe("Error inviting users: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Failed to invite users: ${e.message}"))
        }
    }

    /**
     * Accept an event invitation
     */
    @PostMapping("/{eventUserId}/accept")
    fun acceptInvitation(
        @PathVariable eventUserId: String
    ): ResponseEntity<Any> {
        return try {
            val currentUserId = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            val docRef = firestore.collection(EVENT_USERS_COLLECTION).document(eventUserId)
            val doc = docRef.get().get()

            if (!doc.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "Invitation not found"))
            }

            val eventUser = doc.toObject(EventUser::class.java)
            if (eventUser?.userId != currentUserId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse("FORBIDDEN", "You can only respond to your own invitations"))
            }

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            docRef.update(
                mapOf(
                    "status" to EventUserStatus.ACCEPTED.name,
                    "respondedAt" to now
                )
            ).get()

            logger.info("User $currentUserId accepted invitation to event ${eventUser.eventId}")

            ResponseEntity.ok(mapOf(
                "message" to "Invitation accepted",
                "eventId" to eventUser.eventId
            ))
        } catch (e: Exception) {
            logger.severe("Error accepting invitation: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Failed to accept invitation: ${e.message}"))
        }
    }

    /**
     * Decline an event invitation
     */
    @PostMapping("/{eventUserId}/decline")
    fun declineInvitation(
        @PathVariable eventUserId: String
    ): ResponseEntity<Any> {
        return try {
            val currentUserId = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            val docRef = firestore.collection(EVENT_USERS_COLLECTION).document(eventUserId)
            val doc = docRef.get().get()

            if (!doc.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "Invitation not found"))
            }

            val eventUser = doc.toObject(EventUser::class.java)
            if (eventUser?.userId != currentUserId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse("FORBIDDEN", "You can only respond to your own invitations"))
            }

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            docRef.update(
                mapOf(
                    "status" to EventUserStatus.DECLINED.name,
                    "respondedAt" to now
                )
            ).get()

            logger.info("User $currentUserId declined invitation to event ${eventUser.eventId}")

            ResponseEntity.ok(mapOf(
                "message" to "Invitation declined",
                "eventId" to eventUser.eventId
            ))
        } catch (e: Exception) {
            logger.severe("Error declining invitation: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Failed to decline invitation: ${e.message}"))
        }
    }

    /**
     * Get all attendees for an event
     */
    @GetMapping("/event/{eventId}")
    fun getEventAttendees(
        @PathVariable eventId: String,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<Any> {
        return try {
            SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            var query = firestore.collection(EVENT_USERS_COLLECTION)
                .whereEqualTo("eventId", eventId)

            // Filter by status if provided
            if (status != null) {
                query = query.whereEqualTo("status", status.uppercase())
            }

            val result = query.get().get()
            val eventUsers = result.documents.mapNotNull { doc ->
                val eventUser = doc.toObject(EventUser::class.java)
                // Return eventUser with Firestore document ID
                eventUser?.copy(id = doc.id)
            }

            ResponseEntity.ok(eventUsers)
        } catch (e: Exception) {
            logger.severe("Error getting event attendees: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Failed to get attendees: ${e.message}"))
        }
    }

    /**
     * Get all events for current user
     */
    @GetMapping("/my-events")
    fun getMyEvents(
        @RequestParam(required = false) status: String?
    ): ResponseEntity<Any> {
        return try {
            val currentUserId = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            var query = firestore.collection(EVENT_USERS_COLLECTION)
                .whereEqualTo("userId", currentUserId)

            // Filter by status if provided
            if (status != null) {
                query = query.whereEqualTo("status", status.uppercase())
            }

            val result = query.get().get()
            val eventUsers = result.documents.mapNotNull { doc ->
                doc.toObject(EventUser::class.java)
            }

            // Fetch the actual event details for each
            val events = eventUsers.mapNotNull { eventUser ->
                try {
                    val eventDoc = firestore.collection(EVENTS_COLLECTION)
                        .document(eventUser.eventId)
                        .get()
                        .get()

                    if (eventDoc.exists()) {
                        val event = eventDoc.toObject(Event::class.java)
                        mapOf(
                            "id" to eventDoc.id,
                            "event" to event,
                            "userStatus" to eventUser.status,
                            "userRole" to eventUser.role
                        )
                    } else null
                } catch (e: Exception) {
                    logger.warning("Failed to fetch event ${eventUser.eventId}: ${e.message}")
                    null
                }
            }

            ResponseEntity.ok(events)
        } catch (e: Exception) {
            logger.severe("Error getting user events: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Failed to get events: ${e.message}"))
        }
    }
}

/**
 * Request to invite users to an event
 */
data class InviteUsersRequest(
    val eventId: String,
    val userIds: List<String>
)
