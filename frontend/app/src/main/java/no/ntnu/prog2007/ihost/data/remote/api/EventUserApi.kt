package no.ntnu.prog2007.ihost.data.remote.api

import no.ntnu.prog2007.ihost.data.model.domain.EventUser
import no.ntnu.prog2007.ihost.data.model.dto.EventWithMetadata
import no.ntnu.prog2007.ihost.data.model.dto.InviteUsersRequest
import no.ntnu.prog2007.ihost.data.model.dto.InviteUsersResponse
import no.ntnu.prog2007.ihost.data.remote.config.ApiEndpoints.EVENT_USERS
import retrofit2.http.*

interface EventUserApi {
    @POST("$EVENT_USERS/invite")
    suspend fun inviteUsers(
        @Body request: InviteUsersRequest
    ): InviteUsersResponse

    @POST("$EVENT_USERS/{eventUserId}/accept")
    suspend fun acceptInvitation(
        @Path("eventUserId") eventUserId: String
    ): Map<String, Any>

    @POST("$EVENT_USERS/{eventUserId}/decline")
    suspend fun declineInvitation(
        @Path("eventUserId") eventUserId: String
    ): Map<String, Any>

    @GET("$EVENT_USERS/event/{eventId}")
    suspend fun getEventAttendees(
        @Path("eventId") eventId: String,
        @Query("status") status: String? = null
    ): List<EventUser>

    @GET("$EVENT_USERS/my-events")
    suspend fun getMyEvents(
        @Query("status") status: String? = null
    ): List<EventWithMetadata>
}
