package no.ntnu.prog2007.ihostapi.service

import org.springframework.web.multipart.MultipartFile

/**
 * Service interface for uploading images to Cloudinary
 */
interface CloudinaryService {
    /**
     * Upload an image to Cloudinary
     * @param file The image file to upload
     * @param folder The folder to upload to (default: event_images)
     * @return The secure URL of the uploaded image
     * @throws Exception if upload fails
     */
    fun uploadImage(file: MultipartFile, folder: String = "event_images"): String

    /**
     * Delete an image from Cloudinary
     * @param publicId The public ID of the image to delete
     * @return true if deletion was successful
     */
    fun deleteImage(publicId: String): Boolean
}