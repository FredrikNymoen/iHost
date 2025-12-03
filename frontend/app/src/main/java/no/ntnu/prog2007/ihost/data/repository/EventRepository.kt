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

class EventRepository(
    private val eventApi: EventApi = RetrofitClient.eventApi
) {

    /**
     * Get all events linked to the user
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
     * Get single event by ID
     */
    suspend fun getEventById(eventId: String): Result<EventWithMetadata> {
        return try {
            val eventDto = eventApi.getEventById(eventId)
            val event = mapToEventWithMetadata(eventDto)
            Log.d("EventRepository", "Loaded event: ${event.event.title}")
            Result.success(event)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error loading event with ID: $eventId", e)
            Result.failure(e)
        }
    }

    /**
     * Get event by share code
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
     * Create new event
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
     * Update event
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
     * Delete event
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