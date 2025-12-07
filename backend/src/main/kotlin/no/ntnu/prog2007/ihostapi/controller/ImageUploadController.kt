package no.ntnu.prog2007.ihostapi.controller

import no.ntnu.prog2007.ihostapi.exception.UnauthorizedException
import no.ntnu.prog2007.ihostapi.service.ImageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.logging.Logger

/**
 * REST controller for handling image upload operations.
 *
 * Manages image uploads for events and user profiles using Cloudinary as the storage backend.
 * Cloudinary provides:
 * - Automatic image optimization and transformation
 * - Global CDN for fast delivery
 * - URL-based image manipulation
 *
 * Uploaded images are stored in Cloudinary and their metadata (URL, eventId, etc.)
 * is persisted in Firestore for easy retrieval.
 *
 * @property imageService Business logic service for image operations
 * @see no.ntnu.prog2007.ihostapi.service.ImageService for upload implementation
 * @see no.ntnu.prog2007.ihostapi.config.CloudinaryConfig for Cloudinary configuration
 */
@RestController
@RequestMapping("/api/images")
class ImageUploadController(
    private val imageService: ImageService
) {
    private val logger = Logger.getLogger(ImageUploadController::class.java.name)

    /**
     * Uploads an event image to Cloudinary.
     *
     * Accepts a multipart file upload, stores it in Cloudinary, and saves
     * the image URL and metadata to Firestore. The image is associated with
     * a specific event for gallery/display purposes.
     *
     * @param file The image file to upload (MultipartFile from form data)
     * @param eventId The Firestore document ID of the event
     * @return Image metadata including Cloudinary URL and document ID (HTTP 201)
     */
    @PostMapping("/upload")
    fun uploadEventImage(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("eventId") eventId: String
    ): ResponseEntity<Map<String, String>> {
        getCurrentUserId() // Verify authentication
        val result = imageService.uploadEventImage(file, eventId)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(result)
    }

    /**
     * Retrieves all images associated with an event.
     *
     * Returns a list of image metadata including Cloudinary URLs, upload timestamps,
     * and document IDs. Images can be displayed in an event gallery or slideshow.
     *
     * @param eventId The Firestore document ID of the event
     * @return List of image metadata objects
     */
    @GetMapping("/event/{eventId}")
    fun getEventImages(@PathVariable eventId: String): ResponseEntity<List<Map<String, Any?>?>> {
        getCurrentUserId() // Verify authentication
        val images = imageService.getEventImages(eventId)

        logger.info("Retrieved ${images.size} images for event: $eventId")
        return ResponseEntity.ok(images)
    }

    /**
     * Uploads a user profile photo to Cloudinary.
     *
     * Uploads the image and updates the authenticated user's Firestore document
     * with the new photo URL. Replaces any existing profile photo (old image
     * remains in Cloudinary but is no longer referenced).
     *
     * @param file The profile photo to upload (MultipartFile from form data)
     * @return Success message with the Cloudinary photo URL (HTTP 201)
     */
    @PostMapping("/upload-profile")
    fun uploadProfilePhoto(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        val uid = getCurrentUserId()
        val photoUrl = imageService.uploadProfilePhoto(file, uid)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapOf(
                "message" to "Profile photo uploaded successfully",
                "photoUrl" to photoUrl
            ))
    }

    /**
     * Deletes an image from both Cloudinary and Firestore.
     *
     * Removes the image file from Cloudinary storage and deletes the metadata
     * document from Firestore. This is a permanent deletion and cannot be undone.
     *
     * @param documentId The Firestore document ID of the image metadata
     * @return Success message
     */
    @DeleteMapping("/{documentId}")
    fun deleteImage(@PathVariable documentId: String): ResponseEntity<Map<String, String>> {
        getCurrentUserId() // Verify authentication
        imageService.deleteImage(documentId)

        logger.info("Image deleted: $documentId")
        return ResponseEntity.ok(mapOf("message" to "Image deleted successfully"))
    }

    /**
     * Extracts the Firebase UID from the SecurityContext.
     *
     * The UID is placed in the SecurityContext by [FirebaseTokenFilter]
     * after successfully validating the JWT token.
     *
     * @return Firebase UID of the authenticated user
     * @throws UnauthorizedException if no valid authentication exists
     * @see no.ntnu.prog2007.ihostapi.security.filter.FirebaseTokenFilter
     */
    private fun getCurrentUserId(): String {
        return SecurityContextHolder.getContext().authentication.principal as? String
            ?: throw UnauthorizedException("Token is invalid or missing")
    }
}
