package no.ntnu.prog2007.ihost.data.remote

import com.google.gson.annotations.SerializedName
import no.ntnu.prog2007.ihost.data.model.*
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

    @GET("api/auth/users")
    suspend fun getAllUsers(): List<User>

    @GET("api/auth/user/{uid}")
    suspend fun getUserByUid(
        @Path("uid") uid: String
    ): User

    // Event endpoints
    @GET("api/events")
    suspend fun getAllEvents(): List<EventWithMetadata>

    @GET("api/events/{id}")
    suspend fun getEventById(
        @Path("id") id: String
    ): EventWithMetadata

    @POST("api/events")
    suspend fun createEvent(
        @Body request: CreateEventRequest
    ): EventWithMetadata

    @PUT("api/events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Body request: UpdateEventRequest
    ): EventWithMetadata

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(
        @Path("id") id: String
    ): Map<String, Any>

    @GET("api/stripe/keys")
    suspend fun getKeys(): KeysResponse

    @POST("api/stripe/payment-intent")
    suspend fun createPaymentIntent(
        @Body request: PaymentIntentRequest
    ): PaymentIntentResponse

    @GET("api/events/by-code/{shareCode}")
    suspend fun getEventByCode(
        @Path("shareCode") shareCode: String
    ): EventWithMetadata

    // Event-User endpoints
    @POST("api/event-users/invite")
    suspend fun inviteUsers(
        @Body request: InviteUsersRequest
    ): InviteUsersResponse

    @POST("api/event-users/{eventUserId}/accept")
    suspend fun acceptInvitation(
        @Path("eventUserId") eventUserId: String
    ): Map<String, Any>

    @POST("api/event-users/{eventUserId}/decline")
    suspend fun declineInvitation(
        @Path("eventUserId") eventUserId: String
    ): Map<String, Any>

    @GET("api/event-users/event/{eventId}")
    suspend fun getEventAttendees(
        @Path("eventId") eventId: String,
        @Query("status") status: String? = null
    ): List<EventUser>

    @GET("api/event-users/my-events")
    suspend fun getMyEvents(
        @Query("status") status: String? = null
    ): List<EventWithMetadata>

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

    // Username availability check
    @GET("api/auth/username-available/{username}")
    suspend fun isUsernameAvailable(@Path("username") username: String): Map<String, Boolean>

    // Update user profile
    @PUT("api/auth/user/{uid}")
    suspend fun updateUserProfile(
        @Path("uid") uid: String,
        @Body request: UpdateUserRequest
    ): User
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
