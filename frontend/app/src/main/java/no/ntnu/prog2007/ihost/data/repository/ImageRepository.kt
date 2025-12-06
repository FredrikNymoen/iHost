package no.ntnu.prog2007.ihost.data.repository

import android.util.Log
import no.ntnu.prog2007.ihost.data.model.domain.EventImage
import no.ntnu.prog2007.ihost.data.model.dto.EventImageResponse
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient
import no.ntnu.prog2007.ihost.data.remote.api.ImageApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Repository for image upload and management operations
 *
 * Handles image uploads to Cloudinary through the backend API.
 * Supports both event images and user profile photos.
 * All images are uploaded as multipart form data.
 *
 * @property imageApi The Retrofit API interface for image endpoints
 */
class ImageRepository(
    private val imageApi: ImageApi = RetrofitClient.imageApi
) {

    /**
     * Upload an image for an event
     *
     * Uploads an image file to Cloudinary and associates it with the event.
     * The backend returns the Cloudinary URL which can be stored in the database.
     *
     * @param imageFile The image file to upload from local storage
     * @param eventId The ID of the event to associate the image with
     * @return Result containing the Cloudinary image URL, or error
     */
    suspend fun uploadEventImage(imageFile: File, eventId: String): Result<String> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val eventIdBody = eventId.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = imageApi.uploadEventImage(filePart, eventIdBody)
            Log.d("ImageRepository", "Image uploaded successfully: ${response.imageUrl}")
            Result.success(response.imageUrl)
        } catch (e: Exception) {
            Log.e("ImageRepository", "Error uploading event image", e)
            Result.failure(e)
        }
    }

    /**
     * Get all images for a specific event
     *
     * Fetches all images associated with an event. Currently, the app
     * supports one image per event, but the backend is designed to
     * support multiple images.
     *
     * @param eventId The ID of the event
     * @return Result containing list of event images, or error
     */
    suspend fun getEventImages(eventId: String): Result<List<EventImage>> {
        return try {
            val imagesDto = imageApi.getEventImages(eventId)
            val images = imagesDto.map { mapToEventImage(it) }
            Log.d("ImageRepository", "Loaded ${images.size} images for event $eventId")
            Result.success(images)
        } catch (e: Exception) {
            Log.e("ImageRepository", "Error loading event images", e)
            Result.failure(e)
        }
    }

    private fun mapToEventImage(dto: EventImageResponse): EventImage {
        return EventImage(
            id = dto.id,
            path = dto.path,
            eventId = dto.eventId,
            createdAt = dto.createdAt
        )
    }

    /**
     * Upload a profile photo for the current user
     *
     * Uploads a profile photo to Cloudinary and returns the URL.
     * The URL should then be stored in the user profile using UserRepository.
     *
     * @param imageFile The profile photo file to upload
     * @return Result containing the Cloudinary photo URL, or error
     */
    suspend fun uploadProfilePhoto(imageFile: File): Result<String> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            val response = imageApi.uploadProfilePhoto(filePart)
            Log.d("ImageRepository", "Profile photo uploaded successfully: ${response.photoUrl}")
            Result.success(response.photoUrl)
        } catch (e: Exception) {
            Log.e("ImageRepository", "Error uploading profile photo", e)
            Result.failure(e)
        }
    }

    /**
     * Delete an event image by document ID
     * @param eventImageDocumentId The Firestore document ID of the event image to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteEventImage(eventImageDocumentId: String): Result<Unit> {
        return try {
            imageApi.deleteEventImage(eventImageDocumentId)
            Log.d("ImageRepository", "Event image deleted successfully: $eventImageDocumentId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ImageRepository", "Error deleting event image", e)
            Result.failure(e)
        }
    }
}
