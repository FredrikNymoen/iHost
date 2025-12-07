package no.ntnu.prog2007.ihost.data.repository

import android.util.Log
import no.ntnu.prog2007.ihost.data.model.domain.Event
import no.ntnu.prog2007.ihost.data.model.domain.EventWithMetadata
import no.ntnu.prog2007.ihost.data.model.dto.CreateEventRequest
import no.ntnu.prog2007.ihost.data.model.dto.EventResponse
import no.ntnu.prog2007.ihost.data.model.dto.EventWithMetadataResponse
import no.ntnu.prog2007.ihost.data.model.dto.UpdateEventRequest
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient
import no.ntnu.prog2007.ihost.data.remote.api.EventApi

/**
 * Repository for event-related API operations
 *
 * Handles communication with the backend Event API and maps DTOs
 * to domain models. All methods return Result objects for proper
 * error handling.
 *
 * @property eventApi The Retrofit API interface for event endpoints
 */
class EventRepository(
    private val eventApi: EventApi = RetrofitClient.eventApi
) {

    /**
     * Get all events linked to the current user
     *
     * Returns events where the user is creator, accepted attendee,
     * or has a pending invitation. Each event includes metadata
     * about the user's status and role.
     *
     * @return Result containing list of events with user metadata, or error
     */
    suspend fun getUserEvents(): Result<List<EventWithMetadata>> {
        return try {
            val eventsDto = eventApi.getAllEvents()
            val events = eventsDto.map { mapToEventWithMetadata(it) }
            Log.d("EventRepository", "Loaded ${events.size} events")
            Result.success(events)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error loading events", e)
            Result.failure(e)
        }
    }


    /**
     * Get event by share code
     *
     * Fetches an event using its unique 6-character share code.
     * Creates an event_user with PENDING status for the current user
     * if they're not already associated with the event.
     *
     * @param shareCode The 6-character share code
     * @return Result containing event with user metadata, or error
     */
    suspend fun getEventByCode(shareCode: String): Result<EventWithMetadata> {
        return try {
            val eventDto = eventApi.getEventByCode(shareCode)
            val event = mapToEventWithMetadata(eventDto)
            Log.d("EventRepository", "Loaded event by code: ${event.event.title}")
            Result.success(event)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error loading event with code: $shareCode", e)
            Result.failure(e)
        }
    }

    /**
     * Create a new event
     *
     * Creates an event on the backend. The creator is automatically
     * added as an attendee with CREATOR role. A unique share code
     * is generated for the event.
     *
     * @param title Event title (required)
     * @param description Event description (optional)
     * @param eventDate Event date in ISO format (required)
     * @param eventTime Event time (optional)
     * @param location Event location address (optional)
     * @param free Whether the event is free (default: true)
     * @param price Event price in NOK (only used if free=false)
     * @return Result containing created event with user metadata, or error
     */
    suspend fun createEvent(
        title: String,
        description: String? = null,
        eventDate: String,
        eventTime: String? = null,
        location: String? = null,
        free: Boolean = true,
        price: Double = 0.0
    ): Result<EventWithMetadata> {
        return try {
            val request = CreateEventRequest(
                title = title,
                description = description,
                eventDate = eventDate,
                eventTime = eventTime,
                location = location,
                free = free,
                price = price
            )
            val eventDto = eventApi.createEvent(request)
            val event = mapToEventWithMetadata(eventDto)
            Log.d("EventRepository", "Created event: ${event.event.title}")
            Result.success(event)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error creating event", e)
            Result.failure(e)
        }
    }

    /**
     * Update an existing event
     *
     * Only the event creator can update an event. All parameters are
     * optional - only provided values will be updated.
     *
     * @param eventId The ID of the event to update
     * @param title New event title (optional)
     * @param description New event description (optional)
     * @param eventDate New event date (optional)
     * @param eventTime New event time (optional)
     * @param location New event location (optional)
     * @return Result containing updated event with user metadata, or error
     */
    suspend fun updateEvent(
        eventId: String,
        title: String? = null,
        description: String? = null,
        eventDate: String? = null,
        eventTime: String? = null,
        location: String? = null
    ): Result<EventWithMetadata> {
        return try {
            val request = UpdateEventRequest(
                title = title,
                description = description,
                eventDate = eventDate,
                eventTime = eventTime,
                location = location
            )
            val eventDto = eventApi.updateEvent(eventId, request)
            val event = mapToEventWithMetadata(eventDto)
            Log.d("EventRepository", "Updated event: $eventId")
            Result.success(event)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error updating event", e)
            Result.failure(e)
        }
    }

    /**
     * Delete an event
     *
     * Only the event creator can delete an event. Deletes all associated
     * data including event_users and images.
     *
     * @param eventId The ID of the event to delete
     * @return Result indicating success or error
     */
    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            eventApi.deleteEvent(eventId)
            Log.d("EventRepository", "Deleted event: $eventId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error deleting event", e)
            Result.failure(e)
        }
    }

    private fun mapToEventWithMetadata(dto: EventWithMetadataResponse): EventWithMetadata {
        return EventWithMetadata(
            id = dto.id,
            event = Event(
                title = dto.event.title,
                description = dto.event.description,
                eventDate = dto.event.eventDate,
                eventTime = dto.event.eventTime,
                location = dto.event.location,
                creatorUid = dto.event.creatorUid,
                free = dto.event.free,
                price = dto.event.price,
                createdAt = dto.event.createdAt,
                updatedAt = dto.event.updatedAt,
                shareCode = dto.event.shareCode
            ),
            userStatus = dto.userStatus,
            userRole = dto.userRole
        )
    }
}