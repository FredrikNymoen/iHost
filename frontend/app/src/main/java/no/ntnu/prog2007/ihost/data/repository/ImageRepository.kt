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

class ImageRepository(
    private val imageApi: ImageApi = RetrofitClient.imageApi
) {

    /**
     * Upload an image for an event
     * @param imageFile The image file to upload
     * @param eventId The ID of the event
     * @return Result containing the uploaded image URL
     */
    suspend fun uploadEventImage(imageFile: File, eventId: String): Result<String> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val eventIdBody = eventId.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = imageApi.uploadImage(filePart, eventIdBody)
            Log.d("ImageRepository", "Image uploaded successfully: ${response.imageUrl}")
            Result.success(response.imageUrl)
        } catch (e: Exception) {
            Log.e("ImageRepository", "Error uploading event image", e)
            Result.failure(e)
        }
    }

    /**
     * Get all images for a specific event
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
            path = dto.path,
            eventId = dto.eventId,
            createdAt = dto.createdAt
        )
    }

    /**
     * Upload a profile photo for the current user
     * @param imageFile The profile photo to upload
     * @return Result containing the uploaded photo URL
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
}
