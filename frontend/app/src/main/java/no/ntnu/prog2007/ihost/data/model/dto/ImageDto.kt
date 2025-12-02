package no.ntnu.prog2007.ihost.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Response from image upload endpoint
 */
data class ImageUploadResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("eventId")
    val eventId: String
)

/**
 * Event image data
 */
data class EventImage(
    @SerializedName("path")
    val path: String,
    @SerializedName("eventId")
    val eventId: String,
    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * Response from profile photo upload
 */
data class ProfilePhotoUploadResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("photoUrl")
    val photoUrl: String
)
