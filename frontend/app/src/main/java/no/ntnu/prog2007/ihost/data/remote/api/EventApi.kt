package no.ntnu.prog2007.ihost.data.remote.api

import no.ntnu.prog2007.ihost.data.model.dto.EventWithMetadata
import no.ntnu.prog2007.ihost.data.model.dto.CreateEventRequest
import no.ntnu.prog2007.ihost.data.model.dto.UpdateEventRequest
import no.ntnu.prog2007.ihost.data.remote.config.ApiEndpoints.EVENTS
import retrofit2.http.*

interface EventApi {
    @GET(EVENTS)
    suspend fun getAllEvents(): List<EventWithMetadata>

    @GET("$EVENTS/{id}")
    suspend fun getEventById(
        @Path("id") id: String
    ): EventWithMetadata

    @POST(EVENTS)
    suspend fun createEvent(
        @Body request: CreateEventRequest
    ): EventWithMetadata

    @PUT("$EVENTS/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Body request: UpdateEventRequest
    ): EventWithMetadata

    @DELETE("$EVENTS/{id}")
    suspend fun deleteEvent(
        @Path("id") id: String
    ): Map<String, Any>

    @GET("$EVENTS/by-code/{shareCode}")
    suspend fun getEventByCode(
        @Path("shareCode") shareCode: String
    ): EventWithMetadata
}
