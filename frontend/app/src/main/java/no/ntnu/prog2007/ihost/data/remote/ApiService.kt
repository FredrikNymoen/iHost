package no.ntnu.prog2007.ihost.data.remote

import no.ntnu.prog2007.ihost.data.model.AuthResponse
import no.ntnu.prog2007.ihost.data.model.CreateEventRequest
import no.ntnu.prog2007.ihost.data.model.CreateUserRequest
import no.ntnu.prog2007.ihost.data.model.Event
import no.ntnu.prog2007.ihost.data.model.KeysResponse
import no.ntnu.prog2007.ihost.data.model.User
import retrofit2.http.*

interface ApiService {
    // Auth endpoints
    @POST("api/auth/register")
    suspend fun registerUser(
        @Body request: CreateUserRequest
    ): AuthResponse

    //@GET("api/auth/verify")
    //suspend fun verifyAuth(): User

    @GET("api/auth/user/{uid}")
    suspend fun getUserByUid(
        @Path("uid") uid: String
    ): User

    // Event endpoints
    @GET("api/events")
    suspend fun getAllEvents(): List<Event>

    @GET("api/events/{id}")
    suspend fun getEventById(
        @Path("id") id: String
    ): Event

    @POST("api/events")
    suspend fun createEvent(
        @Body request: CreateEventRequest
    ): Event

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(
        @Path("id") id: String
    ): Map<String, Any>

    @POST("api/events/{id}/join")
    suspend fun joinEvent(
        @Path("id") id: String
    ): Event

    @POST("api/events/{id}/leave")
    suspend fun leaveEvent(
        @Path("id") id: String
    ): Event

    @GET("api/stripe/keys")
    suspend fun getKeys(): KeysResponse

}
