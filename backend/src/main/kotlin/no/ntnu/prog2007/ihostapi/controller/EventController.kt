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

@RestController
@RequestMapping("/api/events")
class EventController(
    private val eventService: EventService
) {
    private val logger = Logger.getLogger(EventController::class.java.name)

    /**
     * Get all events for current user
     */
    @GetMapping
    fun getAllEvents(): ResponseEntity<List<Map<String, Any?>>> {
        val uid = getCurrentUserId()
        val events = eventService.getAllEventsForUser(uid)
        logger.info("Retrieved ${events.size} events for user: $uid")
        return ResponseEntity.ok(events)
    }

    /**
     * Get event by ID
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
     * Create a new event
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
     * Update an event
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
     * Delete an event
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
     * Find event by share code
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
     * Helper function to get current authenticated user ID
     */
    private fun getCurrentUserId(): String {
        return SecurityContextHolder.getContext().authentication.principal as? String
            ?: throw UnauthorizedException("Token is invalid or missing")
    }
}
