package no.ntnu.prog2007.ihost.data.repository

import android.util.Log
import no.ntnu.prog2007.ihost.data.model.dto.EventWithMetadata
import no.ntnu.prog2007.ihost.data.model.dto.CreateEventRequest
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
            val events = eventApi.getAllEvents()
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
            val event = eventApi.getEventById(eventId)
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
            val event = eventApi.getEventByCode(shareCode)
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
            val event = eventApi.createEvent(request)
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
            val event = eventApi.updateEvent(eventId, request)
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
}