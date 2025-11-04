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
import java.util.UUID
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
        @RequestParam("eventId", required = false) eventId: String?
    ): ResponseEntity<Any> {
        return try {
            val uid = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            logger.info("User $uid uploading image: ${file.originalFilename}")

            // Upload to Cloudinary
            val imageUrl = cloudinaryService.uploadImage(file)

            // Create metadata document
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val timestamp = now.format(formatter)

            val imageMetadata = mapOf(
                "id" to UUID.randomUUID().toString(),
                "path" to imageUrl,
                "eventId" to (eventId ?: ""),
                "uploadedBy" to uid,
                "createdAt" to timestamp,
                "originalFilename" to (file.originalFilename ?: "unknown")
            )

            // Store in Firestore
            val docRef = firestore.collection(EVENT_IMAGES_COLLECTION)
                .document()

            docRef.set(imageMetadata).get()

            logger.info("Image uploaded and metadata stored: ${imageMetadata["id"]}")

            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapOf(
                    "message" to "Image uploaded successfully",
                    "imageUrl" to imageUrl,
                    "documentId" to docRef.id,
                    "metadata" to imageMetadata
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
            val uid = SecurityContextHolder.getContext().authentication.principal as? String
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

            val uploadedBy = imageDoc.getString("uploadedBy")
            if (uploadedBy != uid) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse("FORBIDDEN", "Only the uploader can delete this image"))
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

            logger.info("Image deleted: $documentId by user: $uid")
            ResponseEntity.ok(mapOf("message" to "Image deleted successfully"))
        } catch (e: Exception) {
            logger.warning("Error deleting image $documentId: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not delete image"))
        }
    }
}