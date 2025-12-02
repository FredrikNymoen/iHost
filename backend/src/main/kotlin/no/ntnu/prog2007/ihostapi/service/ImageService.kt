package no.ntnu.prog2007.ihostapi.service

import org.springframework.web.multipart.MultipartFile

/**
 * Service interface for image operations
 */
interface ImageService {
    fun uploadEventImage(file: MultipartFile, eventId: String): Map<String, String>
    fun uploadProfilePhoto(file: MultipartFile, userId: String): String
    fun getEventImages(eventId: String): List<Map<String, Any?>?>
    fun deleteImage(documentId: String)
}
