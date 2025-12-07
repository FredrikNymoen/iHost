package no.ntnu.prog2007.ihostapi.service

import no.ntnu.prog2007.ihostapi.model.dto.CreateEventRequest
import no.ntnu.prog2007.ihostapi.model.dto.UpdateEventRequest
import no.ntnu.prog2007.ihostapi.model.entity.Event

/**
 * Service interface for Event operations
 */
interface EventService {
    /**
     * Get all events associated with a user
     * @param userId The ID of the user
     * @return List of event data maps containing event details and user's role/status
     */
    fun getAllEventsForUser(userId: String): List<Map<String, Any?>>

    /**
     * Get event details by ID
     * @param eventId The ID of the event
     * @param userId The ID of the user requesting the event
     * @return Map containing event details and user's role/status, or null if not found
     */
    fun getEventById(eventId: String, userId: String): Map<String, Any?>?

    /**
     * Create a new event
     * @param request The event creation request with event details
     * @param creatorUid The ID of the user creating the event
     * @return Pair of event ID and the created Event entity
     */
    fun createEvent(request: CreateEventRequest, creatorUid: String): Pair<String, Event>

    /**
     * Update an existing event
     * @param eventId The ID of the event to update
     * @param request The update request with new event details
     * @param userId The ID of the user updating the event
     * @return The updated Event entity
     * @throws IllegalArgumentException if user is not the event creator
     */
    fun updateEvent(eventId: String, request: UpdateEventRequest, userId: String): Event

    /**
     * Delete an event and its associated event-user relationships
     * @param eventId The ID of the event to delete
     * @param userId The ID of the user deleting the event
     * @return The number of event-user relationships deleted
     * @throws IllegalArgumentException if user is not the event creator
     */
    fun deleteEvent(eventId: String, userId: String): Int

    /**
     * Find an event by its share code
     * @param shareCode The unique share code of the event
     * @param userId The ID of the user searching for the event
     * @return Map containing event details and user's role/status, or null if not found
     */
    fun findEventByShareCode(shareCode: String, userId: String): Map<String, Any?>?
}
