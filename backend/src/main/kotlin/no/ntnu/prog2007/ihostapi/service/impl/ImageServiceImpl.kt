package no.ntnu.prog2007.ihostapi.service.impl

import no.ntnu.prog2007.ihostapi.exception.ResourceNotFoundException
import no.ntnu.prog2007.ihostapi.repository.ImageRepository
import no.ntnu.prog2007.ihostapi.repository.UserRepository
import no.ntnu.prog2007.ihostapi.service.CloudinaryService
import no.ntnu.prog2007.ihostapi.service.ImageService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.logging.Logger

/**
 * Service implementation for image operations
 */
@Service
class ImageServiceImpl(
    private val cloudinaryService: CloudinaryService,
    private val imageRepository: ImageRepository,
    private val userRepository: UserRepository
) : ImageService {
    private val logger = Logger.getLogger(ImageServiceImpl::class.java.name)

    override fun uploadEventImage(file: MultipartFile, eventId: String): Map<String, String> {
        logger.info("Uploading image for event: $eventId")

        // Upload to Cloudinary
        val imageUrl = cloudinaryService.uploadImage(file)

        // Store metadata in Firestore
        imageRepository.saveEventImage(eventId, imageUrl)

        logger.info("Image uploaded and metadata stored for event: $eventId")

        return mapOf(
            "message" to "Image uploaded successfully",
            "imageUrl" to imageUrl,
            "eventId" to eventId
        )
    }

    override fun uploadProfilePhoto(file: MultipartFile, userId: String): String {
        logger.info("Uploading profile photo for user: $userId")

        // Get user to check if exists
        val user = userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User not found")

        // Delete old photo from Cloudinary if it exists
        val oldPhotoUrl = user.photoUrl
        if (!oldPhotoUrl.isNullOrBlank()) {
            try {
                // Extract public ID from URL (format: .../folder/publicId.extension)
                val publicId = oldPhotoUrl.substringAfterLast("/").substringBeforeLast(".")
                val folder = "user_images"
                cloudinaryService.deleteImage("$folder/$publicId")
                logger.info("Deleted old profile photo from Cloudinary: $oldPhotoUrl")
            } catch (e: Exception) {
                logger.warning("Failed to delete old profile photo, continuing with upload: ${e.message}")
            }
        }

        // Upload new photo to Cloudinary in user_images folder
        val photoUrl = cloudinaryService.uploadImage(file, folder = "user_images")

        logger.info("Profile photo uploaded to Cloudinary: $photoUrl")

        // Update user document with new photoUrl
        userRepository.update(userId, mapOf("photoUrl" to photoUrl))

        logger.info("User document updated with new photoUrl for user: $userId")

        return photoUrl
    }

    override fun getEventImages(eventId: String): List<Map<String, Any?>?> {
        val images = imageRepository.findEventImages(eventId)
        logger.info("Retrieved ${images.size} images for event: $eventId")
        return images
    }

    override fun deleteImage(documentId: String) {
        val imageData = imageRepository.findById(documentId)
            ?: throw ResourceNotFoundException("Image not found")

        // Extract public ID from Cloudinary URL to delete from Cloudinary
        val imageUrl = imageData["path"] as? String
        if (imageUrl != null) {
            // Extract public ID from URL (format: .../folder/publicId.extension)
            val publicId = imageUrl.substringAfterLast("/").substringBeforeLast(".")
            val folder = "event_images"
            cloudinaryService.deleteImage("$folder/$publicId")
        }

        // Delete from Firestore
        imageRepository.delete(documentId)

        logger.info("Image deleted: $documentId")
    }
}
