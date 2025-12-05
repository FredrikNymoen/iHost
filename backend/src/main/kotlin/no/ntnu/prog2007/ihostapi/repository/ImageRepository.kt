package no.ntnu.prog2007.ihostapi.repository

import com.google.cloud.firestore.Firestore
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Repository for image metadata in Firestore
 */
@Repository
class ImageRepository(
    private val firestore: Firestore
) {
    companion object {
        const val EVENT_IMAGES_COLLECTION = "event_images"
    }

    fun saveEventImage(eventId: String, imageUrl: String): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val timestamp = now.format(formatter)

        val imageMetadata = mapOf(
            "path" to imageUrl,
            "eventId" to eventId,
            "createdAt" to timestamp
        )

        val docRef = firestore.collection(EVENT_IMAGES_COLLECTION).document()
        docRef.set(imageMetadata).get()

        return docRef.id
    }

    fun findEventImages(eventId: String): List<Map<String, Any?>> {
        val query = firestore.collection(EVENT_IMAGES_COLLECTION)
            .whereEqualTo("eventId", eventId)
            .get()
            .get()

        return query.documents.map { doc ->
            val data = doc.data?.toMutableMap() ?: mutableMapOf()
            data["id"] = doc.id
            data
        }
    }

    fun findById(documentId: String): Map<String, Any?>? {
        val doc = firestore.collection(EVENT_IMAGES_COLLECTION)
            .document(documentId)
            .get()
            .get()

        return if (doc.exists()) {
            val data = doc.data?.toMutableMap() ?: mutableMapOf()
            data["id"] = doc.id
            data
        } else {
            null
        }
    }

    fun delete(documentId: String): Boolean {
        return try {
            firestore.collection(EVENT_IMAGES_COLLECTION)
                .document(documentId)
                .delete()
                .get()
            true
        } catch (e: Exception) {
            false
        }
    }
}
