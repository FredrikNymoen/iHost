package no.ntnu.prog2007.ihostapi.controller

import jakarta.validation.Valid
import no.ntnu.prog2007.ihostapi.exception.UnauthorizedException
import no.ntnu.prog2007.ihostapi.model.dto.*
import no.ntnu.prog2007.ihostapi.model.entity.EventUserRole
import no.ntnu.prog2007.ihostapi.model.entity.EventUserStatus
import no.ntnu.prog2007.ihostapi.service.EventService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

/**
 * REST controller for event management operations.
 *
 * Handles all event-related HTTP requests including CRUD operations and event discovery.
 * All endpoints require Firebase JWT authentication except where noted in [SecurityConfig].
 *
 * Events are central to the iHost application, allowing users to:
 * - Create events with location, time, and fee information
 * - Share events via unique 6-digit codes
 * - Manage event details and participants
 * - Delete events they created
 *
 * @property eventService Business logic service for event operations
 * @see no.ntnu.prog2007.ihostapi.service.EventService for business logic implementation
 * @see no.ntnu.prog2007.ihostapi.model.entity.Event for event data model
 */
@RestController
@RequestMapping("/api/events")
class EventController(
    private val eventService: EventService
) {
    private val logger = Logger.getLogger(EventController::class.java.name)

    /**
     * Retrieves all events associated with the authenticated user.
     *
     * Returns events where the user is:
     * - The creator
     * - An invited participant
     * - An accepted participant
     *
     * Each event includes user-specific metadata like their status and role.
     *
     * @return List of events with user relationship data
     */
    @GetMapping
    fun getUserEvents(): ResponseEntity<List<Map<String, Any?>>> {
        val uid = getCurrentUserId()
        val events = eventService.getAllEventsForUser(uid)
        logger.info("Retrieved ${events.size} events for user: $uid")
        return ResponseEntity.ok(events)
    }

    /**
     * Retrieves detailed information for a specific event.
     *
     * Includes the event data plus the authenticated user's relationship
     * to the event (status and role). Returns 404 if event doesn't exist.
     *
     * @param id The Firestore document ID of the event
     * @return Event details with user status and role
     * @throws IllegalArgumentException if event is not found
     */
    @GetMapping("/{id}")
    fun getEventById(@PathVariable id: String): ResponseEntity<Map<String, Any?>> {
        val uid = getCurrentUserId()
        val eventData = eventService.getEventById(id, uid)
            ?: throw IllegalArgumentException("Event not found")

        logger.info("Retrieved event: $id for user: $uid")
        return ResponseEntity.ok(eventData)
    }

    /**
     * Creates a new event with the authenticated user as creator.
     *
     * The service automatically:
     * - Generates a unique 6-digit share code for inviting participants
     * - Creates an EventUser relationship with CREATOR status and role
     * - Validates location coordinates and event details
     *
     * @param request Event creation data including title, location, time, and fee
     * @return Created event with generated ID, share code, and creator status (HTTP 201)
     */
    @PostMapping
    fun createEvent(@Valid @RequestBody request: CreateEventRequest): ResponseEntity<Map<String, Any?>> {
        val uid = getCurrentUserId()
        val (eventId, event) = eventService.createEvent(request, uid)

        logger.info("Event created with ID: $eventId by user: $uid")
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapOf(
                "id" to eventId,
                "event" to event,
                "userStatus" to EventUserStatus.CREATOR,
                "userRole" to EventUserRole.CREATOR
            ))
    }

    /**
     * Updates an existing event's details.
     *
     * Only the event creator can update the event. The service verifies
     * creator permissions before allowing modifications. Share code
     * cannot be changed through this endpoint.
     *
     * @param id The Firestore document ID of the event to update
     * @param request Updated event data (partial updates supported)
     * @return Updated event data with user status and role
     * @throws ForbiddenException if user is not the event creator
     */
    @PutMapping("/{id}")
    fun updateEvent(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateEventRequest
    ): ResponseEntity<Map<String, Any?>> {
        val uid = getCurrentUserId()
        val updatedEvent = eventService.updateEvent(id, request, uid)

        logger.info("Event updated: $id by user: $uid")
        return ResponseEntity.ok(mapOf(
            "id" to id,
            "event" to updatedEvent,
            "userStatus" to EventUserStatus.CREATOR,
            "userRole" to EventUserRole.CREATOR
        ))
    }

    /**
     * Deletes an event and all associated relationships.
     *
     * Only the event creator can delete the event. This operation:
     * - Removes the event document from Firestore
     * - Cascades deletion to all EventUser relationships
     * - Does NOT delete associated images (future enhancement)
     *
     * @param id The Firestore document ID of the event to delete
     * @return Success message with count of deleted EventUser records
     * @throws ForbiddenException if user is not the event creator
     */
    @DeleteMapping("/{id}")
    fun deleteEvent(@PathVariable id: String): ResponseEntity<Map<String, Any>> {
        val uid = getCurrentUserId()
        val deletedCount = eventService.deleteEvent(id, uid)

        logger.info("Event deleted: $id by user: $uid with $deletedCount related event_users")
        return ResponseEntity.ok(mapOf(
            "message" to "Event deleted successfully",
            "deletedEventUsers" to deletedCount
        ))
    }

    /**
     * Finds an event by its unique share code.
     *
     * Share codes are 6-digit numeric strings generated when events are created.
     * This endpoint allows users to discover events they want to join without
     * knowing the event ID. Returns event details with user's current relationship.
     *
     * @param shareCode The 6-digit share code to search for
     * @return Event details with user status and role
     * @throws IllegalArgumentException if no event with the given code exists
     */
    @GetMapping("/by-code/{shareCode}")
    fun findEventByCode(@PathVariable shareCode: String): ResponseEntity<Map<String, Any?>> {
        val uid = getCurrentUserId()
        val eventData = eventService.findEventByShareCode(shareCode, uid)
            ?: throw IllegalArgumentException("No event found with code: $shareCode")

        logger.info("Event found by code $shareCode for user: $uid")
        return ResponseEntity.ok(eventData)
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
