package no.ntnu.prog2007.ihost.data.remote.api

import no.ntnu.prog2007.ihost.data.model.dto.EventImageResponse
import no.ntnu.prog2007.ihost.data.model.dto.EventImageUploadResponse
import no.ntnu.prog2007.ihost.data.model.dto.ProfilePhotoUploadResponse
import no.ntnu.prog2007.ihost.data.remote.config.ApiEndpoints.IMAGES
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ImageApi {
    @Multipart
    @POST("$IMAGES/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("eventId") eventId: RequestBody
    ): EventImageUploadResponse

    @GET("$IMAGES/event/{eventId}")
    suspend fun getEventImages(
        @Path("eventId") eventId: String
    ): List<EventImageResponse>

    @Multipart
    @POST("$IMAGES/upload-profile")
    suspend fun uploadProfilePhoto(
        @Part file: MultipartBody.Part
    ): ProfilePhotoUploadResponse
}
