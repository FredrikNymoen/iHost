package no.ntnu.prog2007.ihost.data.remote.api

import no.ntnu.prog2007.ihost.data.model.dto.EventUserResponse
import no.ntnu.prog2007.ihost.data.model.dto.EventWithMetadataResponse
import no.ntnu.prog2007.ihost.data.model.dto.InviteUsersRequest
import no.ntnu.prog2007.ihost.data.model.dto.InviteUsersResponse
import no.ntnu.prog2007.ihost.data.remote.config.ApiEndpoints.EVENT_USERS
import retrofit2.http.*

/**
 * Retrofit API interface for event participation management
 *
 * Handles event_user operations including invitations, acceptances,
 * and attendee management. The event_user entity represents the
 * relationship between a user and an event with status tracking.
 */
interface EventUserApi {
    /**
     * Invite users to an event
     *
     * Creates event_user records with PENDING status for invited users.
     * Only the event creator can invite users.
     *
     * @param request Invitation request with event ID and list of user IDs
     * @return Invitation response with success information
     */
    @POST("$EVENT_USERS/invite")
    suspend fun inviteUsers(
        @Body request: InviteUsersRequest
    ): InviteUsersResponse

    /**
     * Accept an event invitation
     *
     * Updates event_user status from PENDING to ACCEPTED.
     * For paid events, payment must be completed first.
     *
     * @param eventUserId The event_user document ID
     * @return Success response map
     */
    @POST("$EVENT_USERS/{eventUserId}/accept")
    suspend fun acceptInvitation(
        @Path("eventUserId") eventUserId: String
    ): Map<String, Any>

    /**
     * Decline an event invitation
     *
     * Deletes the event_user record, removing the user from the event.
     *
     * @param eventUserId The event_user document ID
     * @return Success response map
     */
    @POST("$EVENT_USERS/{eventUserId}/decline")
    suspend fun declineInvitation(
        @Path("eventUserId") eventUserId: String
    ): Map<String, Any>

    /**
     * Get all attendees for an event
     *
     * Returns all event_user records for the event, optionally filtered by status.
     *
     * @param eventId The event ID
     * @param status Optional status filter (PENDING, ACCEPTED, CREATOR, etc.)
     * @return List of event_user records with user details
     */
    @GET("$EVENT_USERS/event/{eventId}")
    suspend fun getEventAttendees(
        @Path("eventId") eventId: String,
        @Query("status") status: String? = null
    ): List<EventUserResponse>

    /**
     * Get events for the current user
     *
     * Returns all events where the user has an event_user record,
     * optionally filtered by status.
     *
     * @param status Optional status filter (PENDING, ACCEPTED, CREATOR, etc.)
     * @return List of events with user metadata
     */
    @GET("$EVENT_USERS/my-events")
    suspend fun getMyEvents(
        @Query("status") status: String? = null
    ): List<EventWithMetadataResponse>
}
