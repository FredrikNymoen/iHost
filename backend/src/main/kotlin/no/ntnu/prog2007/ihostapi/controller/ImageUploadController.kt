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
 * Controller for handling image uploads to Cloudinary
 */
@RestController
@RequestMapping("/api/images")
class ImageUploadController(
    private val imageService: ImageService
) {
    private val logger = Logger.getLogger(ImageUploadController::class.java.name)

    /**
     * Upload an event image to Cloudinary and store metadata in Firestore
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
     * Get all images for a specific event
     */
    @GetMapping("/event/{eventId}")
    fun getEventImages(@PathVariable eventId: String): ResponseEntity<List<Map<String, Any?>?>> {
        getCurrentUserId() // Verify authentication
        val images = imageService.getEventImages(eventId)

        logger.info("Retrieved ${images.size} images for event: $eventId")
        return ResponseEntity.ok(images)
    }

    /**
     * Upload a profile photo to Cloudinary and update user document
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
     * Delete an image from both Cloudinary and Firestore
     */
    @DeleteMapping("/{documentId}")
    fun deleteImage(@PathVariable documentId: String): ResponseEntity<Map<String, String>> {
        getCurrentUserId() // Verify authentication
        imageService.deleteImage(documentId)

        logger.info("Image deleted: $documentId")
        return ResponseEntity.ok(mapOf("message" to "Image deleted successfully"))
    }

    /**
     * Helper function to get current authenticated user ID
     */
    private fun getCurrentUserId(): String {
        return SecurityContextHolder.getContext().authentication.principal as? String
            ?: throw UnauthorizedException("Token is invalid or missing")
    }
}
