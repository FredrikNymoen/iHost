package no.ntnu.prog2007.ihostapi.service

import org.springframework.web.multipart.MultipartFile

/**
 * Service interface for image operations
 */
interface ImageService {
    /**
     * Upload an event image to Cloudinary and store metadata in Firestore
     * @param file The image file to upload
     * @param eventId The ID of the event the image belongs to
     * @return Map containing success message, image URL, and event ID
     * @throws Exception if upload fails
     */
    fun uploadEventImage(file: MultipartFile, eventId: String): Map<String, String>

    /**
     * Upload a profile photo to Cloudinary and update user document
     * Deletes the old profile photo if one exists
     * @param file The image file to upload
     * @param userId The ID of the user whose profile photo is being updated
     * @return The URL of the uploaded profile photo
     * @throws ResourceNotFoundException if user is not found
     */
    fun uploadProfilePhoto(file: MultipartFile, userId: String): String

    /**
     * Get all images for a specific event
     * @param eventId The ID of the event
     * @return List of image metadata maps from Firestore
     */
    fun getEventImages(eventId: String): List<Map<String, Any?>?>

    /**
     * Delete an image from both Cloudinary and Firestore
     * @param documentId The Firestore document ID of the image
     * @throws ResourceNotFoundException if image is not found
     */
    fun deleteImage(documentId: String)
}
