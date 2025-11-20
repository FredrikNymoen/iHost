package no.ntnu.prog2007.ihostapi.controller

import com.google.cloud.firestore.Firestore
import no.ntnu.prog2007.ihostapi.model.ErrorResponse
import no.ntnu.prog2007.ihostapi.service.CloudinaryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

/**
 * Controller for handling image uploads to Cloudinary
 * Stores image metadata in Firestore collection: event_images
 */
@RestController
@RequestMapping("/api/images")
class ImageUploadController(
    private val cloudinaryService: CloudinaryService,
    private val firestore: Firestore
) {
    private val logger = Logger.getLogger(ImageUploadController::class.java.name)

    companion object {
        const val EVENT_IMAGES_COLLECTION = "event_images"
    }

    /**
     * Upload an image to Cloudinary and store metadata in Firestore
     * Requires valid Firebase JWT token in Authorization header
     *
     * @param file The image file to upload
     * @param eventId Optional event ID to associate with the image
     * @return Response with image URL and metadata
     */
    @PostMapping("/upload")
    fun uploadImage(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("eventId") eventId: String
    ): ResponseEntity<Any> {
        return try {
            if (SecurityContextHolder.getContext().authentication.principal !is String) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            logger.info("Uploading image for event: $eventId")

            // Upload to Cloudinary
            val imageUrl = cloudinaryService.uploadImage(file)

            // Create metadata document with only required fields
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val timestamp = now.format(formatter)

            val imageMetadata = mapOf(
                "path" to imageUrl,
                "eventId" to eventId,
                "createdAt" to timestamp
            )

            // Store in Firestore
            val docRef = firestore.collection(EVENT_IMAGES_COLLECTION)
                .document()

            docRef.set(imageMetadata).get()

            logger.info("Image uploaded and metadata stored for event: $eventId")

            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapOf(
                    "message" to "Image uploaded successfully",
                    "imageUrl" to imageUrl,
                    "eventId" to eventId
                ))
        } catch (e: IllegalArgumentException) {
            logger.warning("Invalid image upload request: ${e.message}")
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse("INVALID_FILE", e.message ?: "Invalid file"))
        } catch (e: Exception) {
            logger.severe("Error uploading image: ${e.message}")
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("UPLOAD_FAILED", e.message ?: "Failed to upload image"))
        }
    }

    /**
     * Get all images for a specific event
     *
     * @param eventId The event ID to get images for
     * @return List of image metadata
     */
    @GetMapping("/event/{eventId}")
    fun getEventImages(
        @PathVariable eventId: String
    ): ResponseEntity<Any> {
        return try {
            SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            val query = firestore.collection(EVENT_IMAGES_COLLECTION)
                .whereEqualTo("eventId", eventId)
                .get()
                .get()

            val images = query.documents.map { doc ->
                doc.data
            }

            logger.info("Retrieved ${images.size} images for event: $eventId")
            ResponseEntity.ok(images)
        } catch (e: Exception) {
            logger.warning("Error getting images for event $eventId: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not retrieve images"))
        }
    }

    /**
     * Upload a profile photo to Cloudinary and update user document
     * Uploads image to Cloudinary and then updates the photoUrl field in user's Firestore document
     *
     * @param file The profile photo file to upload
     * @return Response with photo URL
     */
    @PostMapping("/upload-profile")
    fun uploadProfilePhoto(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        return try {
            // Verify authentication
            val uid = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            logger.info("Uploading profile photo for user: $uid")

            // Upload to Cloudinary in user_images folder
            val photoUrl = cloudinaryService.uploadImage(file, folder = "user_images")

            logger.info("Profile photo uploaded to Cloudinary: $photoUrl")

            // Update user document with new photoUrl
            val userDocRef = firestore.collection("users").document(uid)
            val userDoc = userDocRef.get().get()

            if (!userDoc.exists()) {
                logger.warning("User document not found for UID: $uid")
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "User not found"))
            }

            // Update photoUrl field
            userDocRef.update("photoUrl", photoUrl).get()

            logger.info("User document updated with new photoUrl for user: $uid")

            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapOf(
                    "message" to "Profile photo uploaded successfully",
                    "photoUrl" to photoUrl
                ))
        } catch (e: IllegalArgumentException) {
            logger.warning("Invalid profile photo upload request: ${e.message}")
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse("INVALID_FILE", e.message ?: "Invalid file"))
        } catch (e: Exception) {
            logger.severe("Error uploading profile photo: ${e.message}")
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("UPLOAD_FAILED", e.message ?: "Failed to upload profile photo"))
        }
    }

    /**
     * Delete an image from both Cloudinary and Firestore
     * Only the uploader can delete the image
     *
     * @param documentId The Firestore document ID of the image metadata
     * @return Success message
     */
    @DeleteMapping("/{documentId}")
    fun deleteImage(
        @PathVariable documentId: String
    ): ResponseEntity<Any> {
        return try {
            SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            val imageDoc = firestore.collection(EVENT_IMAGES_COLLECTION)
                .document(documentId)
                .get()
                .get()

            if (!imageDoc.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "Image not found"))
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
            ResponseEntity.ok(mapOf("message" to "Image deleted successfully"))
        } catch (e: Exception) {
            logger.warning("Error deleting image $documentId: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not delete image"))
        }
    }
}