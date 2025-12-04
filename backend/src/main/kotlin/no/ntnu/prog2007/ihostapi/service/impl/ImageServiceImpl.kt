package no.ntnu.prog2007.ihostapi.service.impl

import com.google.cloud.firestore.Firestore
import no.ntnu.prog2007.ihostapi.exception.ResourceNotFoundException
import no.ntnu.prog2007.ihostapi.service.CloudinaryService
import no.ntnu.prog2007.ihostapi.service.ImageService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

/**
 * Service implementation for image operations
 */
@Service
class ImageServiceImpl(
    private val cloudinaryService: CloudinaryService,
    private val firestore: Firestore
) : ImageService {
    private val logger = Logger.getLogger(ImageServiceImpl::class.java.name)

    companion object {
        const val EVENT_IMAGES_COLLECTION = "event_images"
        const val USERS_COLLECTION = "users"
    }

    override fun uploadEventImage(file: MultipartFile, eventId: String): Map<String, String> {
        logger.info("Uploading image for event: $eventId")

        // Upload to Cloudinary
        val imageUrl = cloudinaryService.uploadImage(file)

        // Create metadata document
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val timestamp = now.format(formatter)

        val imageMetadata = mapOf(
            "path" to imageUrl,
            "eventId" to eventId,
            "createdAt" to timestamp
        )

        // Store in Firestore
        val docRef = firestore.collection(EVENT_IMAGES_COLLECTION).document()
        docRef.set(imageMetadata).get()

        logger.info("Image uploaded and metadata stored for event: $eventId")

        return mapOf(
            "message" to "Image uploaded successfully",
            "imageUrl" to imageUrl,
            "eventId" to eventId
        )
    }

    override fun uploadProfilePhoto(file: MultipartFile, userId: String): String {
        logger.info("Uploading profile photo for user: $userId")

        // Get user document to check for existing photoUrl
        val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)
        val userDoc = userDocRef.get().get()

        if (!userDoc.exists()) {
            logger.warning("User document not found for UID: $userId")
            throw ResourceNotFoundException("User not found")
        }

        // Delete old photo from Cloudinary if it exists
        val oldPhotoUrl = userDoc.getString("photoUrl")
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
        userDocRef.update("photoUrl", photoUrl).get()

        logger.info("User document updated with new photoUrl for user: $userId")

        return photoUrl
    }

    override fun getEventImages(eventId: String): List<Map<String, Any?>?> {
        val query = firestore.collection(EVENT_IMAGES_COLLECTION)
            .whereEqualTo("eventId", eventId)
            .get()
            .get()

        val images = query.documents.map { doc ->
            val data = doc.data?.toMutableMap() ?: mutableMapOf()
            data["id"] = doc.id  // Add document ID to the response
            data
        }

        logger.info("Retrieved ${images.size} images for event: $eventId")
        return images
    }

    override fun deleteImage(documentId: String) {
        val imageDoc = firestore.collection(EVENT_IMAGES_COLLECTION)
            .document(documentId)
            .get()
            .get()

        if (!imageDoc.exists()) {
            throw ResourceNotFoundException("Image not found")
        }

        // Extract public ID from Cloudinary URL to delete from Cloudinary
        val imageUrl = imageDoc.getString("path")
        if (imageUrl != null) {
            // Extract public ID from URL (format: .../folder/publicId.extension)
            val publicId = imageUrl.substringAfterLast("/").substringBeforeLast(".")
            val folder = "event_images"
            cloudinaryService.deleteImage("$folder/$publicId")
        }

        // Delete from Firestore
        firestore.collection(EVENT_IMAGES_COLLECTION)
            .document(documentId)
            .delete()
            .get()

        logger.info("Image deleted: $documentId")
    }
}
