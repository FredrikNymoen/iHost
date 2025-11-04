package no.ntnu.prog2007.ihost.data.remote

import com.google.gson.annotations.SerializedName
import no.ntnu.prog2007.ihost.data.model.AuthResponse
import no.ntnu.prog2007.ihost.data.model.CreateEventRequest
import no.ntnu.prog2007.ihost.data.model.CreateUserRequest
import no.ntnu.prog2007.ihost.data.model.Event
import no.ntnu.prog2007.ihost.data.model.KeysResponse
import no.ntnu.prog2007.ihost.data.model.PaymentIntentRequest
import no.ntnu.prog2007.ihost.data.model.PaymentIntentResponse
import no.ntnu.prog2007.ihost.data.model.UpdateEventRequest
import no.ntnu.prog2007.ihost.data.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    @PUT("api/events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Body request: UpdateEventRequest
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

    @POST("api/stripe/payment-intent")
    suspend fun createPaymentIntent(
        @Body request: PaymentIntentRequest
    ): PaymentIntentResponse

    @GET("api/events/by-code/{shareCode}")
    suspend fun getEventByCode(
        @Path("shareCode") shareCode: String
    ): Event

    // Image upload endpoint
    @Multipart
    @POST("api/images/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("eventId") eventId: RequestBody
    ): ImageUploadResponse

    // Get all images for an event
    @GET("api/images/event/{eventId}")
    suspend fun getEventImages(
        @Path("eventId") eventId: String
    ): List<EventImage>
}

data class ImageUploadResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("eventId")
    val eventId: String
)

data class EventImage(
    @SerializedName("path")
    val path: String,
    @SerializedName("eventId")
    val eventId: String,
    @SerializedName("createdAt")
    val createdAt: String
)
