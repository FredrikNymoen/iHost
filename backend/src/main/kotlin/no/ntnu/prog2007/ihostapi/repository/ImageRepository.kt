package no.ntnu.prog2007.ihostapi.repository

import com.google.cloud.firestore.Firestore
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Data access layer for image metadata in Firestore.
 *
 * Stores references to images uploaded to Cloudinary. The actual image files
 * are stored in Cloudinary; this collection only contains metadata linking
 * images to events and tracking upload timestamps.
 *
 * **Data model**: Uses generic Map<String, Any?> instead of a typed entity
 * because the schema is simple and doesn't require validation. The map contains:
 * - `path`: Cloudinary URL of the image
 * - `eventId`: Reference to the associated event
 * - `createdAt`: ISO-8601 timestamp of upload
 * - `id`: Document ID (added during retrieval)
 *
 * @property firestore Injected Firestore client for database operations
 */
@Repository
class ImageRepository(
    private val firestore: Firestore
) {
    companion object {
        /** Firestore collection name for image metadata documents */
        const val EVENT_IMAGES_COLLECTION = "event_images"
    }

    /**
     * Saves metadata for a newly uploaded event image.
     *
     * Creates a document linking the Cloudinary URL to the event,
     * with an automatic timestamp for ordering and auditing.
     *
     * @param eventId Firestore document ID of the associated event
     * @param imageUrl Cloudinary URL where the image is stored
     * @return Generated Firestore document ID for the metadata record
     */
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

    /**
     * Retrieves all image metadata for an event.
     *
     * Returns raw maps with document IDs included, allowing the service
     * layer to build image gallery responses with deletion capabilities.
     *
     * @param eventId Firestore document ID of the event
     * @return List of metadata maps, each containing path, eventId, createdAt, and id
     */
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

    /**
     * Retrieves image metadata by document ID.
     *
     * Used when deleting an image to get the Cloudinary URL
     * for cleanup in the external storage.
     *
     * @param documentId Firestore document ID of the image metadata
     * @return Metadata map if found, null otherwise
     */
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

    /**
     * Deletes image metadata from Firestore.
     *
     * Note: This only removes the metadata record. The actual image
     * in Cloudinary must be deleted separately via CloudinaryService.
     *
     * @param documentId Firestore document ID of the metadata to delete
     * @return true if deletion succeeded, false on error
     */
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
