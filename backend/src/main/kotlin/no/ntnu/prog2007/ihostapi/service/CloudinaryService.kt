package no.ntnu.prog2007.ihostapi.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.logging.Logger

/**
 * Service for uploading images to Cloudinary
 */
@Service
class CloudinaryService(
    private val cloudinary: Cloudinary
) {
    private val logger = Logger.getLogger(CloudinaryService::class.java.name)

    /**
     * Upload an image to Cloudinary
     * @param file The image file to upload
     * @param folder The folder to upload to (default: event_images)
     * @return The secure URL of the uploaded image
     * @throws Exception if upload fails
     */
    fun uploadImage(file: MultipartFile, folder: String = "event_images"): String {
        try {
            logger.info("Uploading image to Cloudinary: ${file.originalFilename}")

            // Validate file
            if (file.isEmpty) {
                throw IllegalArgumentException("File is empty")
            }

            // Validate file type
            val contentType = file.contentType
            if (contentType == null || !contentType.startsWith("image/")) {
                throw IllegalArgumentException("File must be an image")
            }

            // Upload to Cloudinary
            val uploadResult = cloudinary.uploader().upload(
                file.bytes,
                ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image"
                )
            )

            val imageUrl = uploadResult["secure_url"] as String
            logger.info("Image uploaded successfully: $imageUrl")

            return imageUrl
        } catch (e: Exception) {
            logger.severe("Error uploading image to Cloudinary: ${e.message}")
            throw Exception("Failed to upload image: ${e.message}")
        }
    }

    /**
     * Delete an image from Cloudinary
     * @param publicId The public ID of the image to delete
     * @return true if deletion was successful
     */
    fun deleteImage(publicId: String): Boolean {
        return try {
            logger.info("Deleting image from Cloudinary: $publicId")
            val result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
            val resultStatus = result["result"] as String
            logger.info("Image deletion result: $resultStatus")
            resultStatus == "ok"
        } catch (e: Exception) {
            logger.warning("Error deleting image from Cloudinary: ${e.message}")
            false
        }
    }
}