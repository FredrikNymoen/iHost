package no.ntnu.prog2007.ihost.data.remote.api

import no.ntnu.prog2007.ihost.data.model.dto.CreateEventRequest
import no.ntnu.prog2007.ihost.data.model.dto.EventWithMetadataResponse
import no.ntnu.prog2007.ihost.data.model.dto.UpdateEventRequest
import no.ntnu.prog2007.ihost.data.remote.config.ApiEndpoints.EVENTS
import retrofit2.http.*

/**
 * Retrofit API interface for event-related endpoints
 *
 * Defines REST API calls for event CRUD operations.
 * All endpoints require Firebase authentication via the Authorization header
 * (automatically added by FirebaseAuthInterceptor).
 */
interface EventApi {
    /**
     * Get all events for the current user
     *
     * Returns events where the user is creator, accepted attendee, or has pending invitation.
     *
     * @return List of events with user metadata (status and role)
     */
    @GET(EVENTS)
    suspend fun getAllEvents(): List<EventWithMetadataResponse>

    /**
     * Get a specific event by ID
     *
     * @param id The event ID
     * @return Event with user metadata
     */
    @GET("$EVENTS/{id}")
    suspend fun getEventById(
        @Path("id") id: String
    ): EventWithMetadataResponse

    /**
     * Create a new event
     *
     * The current user becomes the event creator and is automatically added as attendee.
     *
     * @param request Event creation request with title, date, location, etc.
     * @return Created event with user metadata
     */
    @POST(EVENTS)
    suspend fun createEvent(
        @Body request: CreateEventRequest
    ): EventWithMetadataResponse

    /**
     * Update an existing event
     *
     * Only the event creator can update an event.
     *
     * @param id The event ID
     * @param request Update request with fields to modify
     * @return Updated event with user metadata
     */
    @PUT("$EVENTS/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Body request: UpdateEventRequest
    ): EventWithMetadataResponse

    /**
     * Delete an event
     *
     * Only the event creator can delete an event.
     * Deletes all associated data (event_users, images).
     *
     * @param id The event ID
     * @return Success message map
     */
    @DELETE("$EVENTS/{id}")
    suspend fun deleteEvent(
        @Path("id") id: String
    ): Map<String, Any>

    /**
     * Get event by share code
     *
     * Fetches an event using its unique 6-character share code.
     * Creates an event_user with PENDING status for the current user if not already associated.
     *
     * @param shareCode The 6-character share code
     * @return Event with user metadata
     */
    @GET("$EVENTS/by-code/{shareCode}")
    suspend fun getEventByCode(
        @Path("shareCode") shareCode: String
    ): EventWithMetadataResponse
}
