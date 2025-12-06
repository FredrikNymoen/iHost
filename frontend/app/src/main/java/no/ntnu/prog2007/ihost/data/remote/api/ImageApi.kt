package no.ntnu.prog2007.ihost.data.remote.api

import no.ntnu.prog2007.ihost.data.model.dto.EventImageResponse
import no.ntnu.prog2007.ihost.data.model.dto.EventImageUploadResponse
import no.ntnu.prog2007.ihost.data.model.dto.ProfilePhotoUploadResponse
import no.ntnu.prog2007.ihost.data.remote.config.ApiEndpoints.IMAGES
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Retrofit API interface for image upload and management
 *
 * Handles image uploads to Cloudinary via the backend.
 * Supports both event images and user profile photos.
 * All uploads use multipart form data.
 */
interface ImageApi {
    /**
     * Upload an event image to Cloudinary
     *
     * Uploads image and associates it with an event in the database.
     *
     * @param file The image file as multipart data
     * @param eventId The event ID to associate the image with
     * @return Upload response containing the Cloudinary image URL
     */
    @Multipart
    @POST("$IMAGES/upload")
    suspend fun uploadEventImage(
        @Part file: MultipartBody.Part,
        @Part("eventId") eventId: RequestBody
    ): EventImageUploadResponse

    /**
     * Get all images for a specific event
     *
     * @param eventId The event ID
     * @return List of event images with Cloudinary URLs
     */
    @GET("$IMAGES/event/{eventId}")
    suspend fun getEventImages(
        @Path("eventId") eventId: String
    ): List<EventImageResponse>

    /**
     * Upload a profile photo to Cloudinary
     *
     * Uploads profile photo for the current user.
     * Does not automatically update the user profile - caller must
     * update the user's photoUrl field separately.
     *
     * @param file The profile photo as multipart data
     * @return Upload response containing the Cloudinary photo URL
     */
    @Multipart
    @POST("$IMAGES/upload-profile")
    suspend fun uploadProfilePhoto(
        @Part file: MultipartBody.Part
    ): ProfilePhotoUploadResponse

    /**
     * Delete an event image
     *
     * Deletes the image from Cloudinary and removes the database record.
     *
     * @param documentId The Firestore document ID of the event image
     */
    @DELETE("$IMAGES/{documentId}")
    suspend fun deleteEventImage(
        @Path("documentId") documentId: String
    )
}
