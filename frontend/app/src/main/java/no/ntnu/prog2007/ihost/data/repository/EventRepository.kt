package no.ntnu.prog2007.ihost.data.repository

import no.ntnu.prog2007.ihost.data.model.CreateEventRequest
import no.ntnu.prog2007.ihost.data.model.Event
import no.ntnu.prog2007.ihost.data.remote.EventImage
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient

class EventRepository(
    private val getAuthToken: suspend () -> String?
) {

    private val apiService = RetrofitClient.apiService

    /**
     * Get all events
     */
    suspend fun getAllEvents(): Result<List<Event>> = try {
        val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
        val events = apiService.getAllEvents()
        Result.success(events)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get single event by ID
     */
    suspend fun getEventById(eventId: String): Result<Event> = try {
        val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
        val event = apiService.getEventById(eventId)
        Result.success(event)
    } catch (e: Exception) {
        Result.failure(e)
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
        imageUrl: String?= null
    ): Result<Event> = try {
        val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
        val request = CreateEventRequest(
            title = title,
            description = description,
            eventDate = eventDate,
            eventTime = eventTime,
            location = location
        )
        val event = apiService.createEvent(request)
        Result.success(event)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Delete event
     */
    suspend fun deleteEvent(eventId: String): Result<Unit> = try {
        val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
        apiService.deleteEvent(eventId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Join event as attendee
     */
    suspend fun joinEvent(eventId: String): Result<Event> = try {
        val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
        val event = apiService.joinEvent(eventId)
        Result.success(event)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Leave event as attendee
     */
    suspend fun leaveEvent(eventId: String): Result<Event> = try {
        val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
        val event = apiService.leaveEvent(eventId)
        Result.success(event)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get all images for a specific event
     */
    suspend fun getEventImages(eventId: String): Result<List<EventImage>> = try {
        val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
        val images = apiService.getEventImages(eventId)
        Result.success(images)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
