package no.ntnu.prog2007.ihostapi.repository

import com.google.cloud.firestore.Firestore
import no.ntnu.prog2007.ihostapi.model.entity.Event
import org.springframework.stereotype.Repository

/**
 * Data access layer for Event entities in Firestore.
 *
 * Manages the `events` collection where each document represents a user-created event.
 * Unlike users, events use auto-generated document IDs since there's no external
 * identifier to use as the primary key.
 *
 * **Return pattern**: Methods that create documents return `Pair<String, Event>`
 * where the String is the auto-generated document ID. This allows services to
 * include the ID in API responses without a second database read.
 *
 * @property firestore Injected Firestore client for database operations
 */
@Repository
class EventRepository(
    private val firestore: Firestore
) {
    companion object {
        /** Firestore collection name for event documents */
        const val COLLECTION_NAME = "events"
    }

    /**
     * Retrieves an event by its document ID.
     *
     * @param id Firestore document ID of the event
     * @return Event entity if found, null otherwise
     */
    fun findById(id: String): Event? {
        val doc = firestore.collection(COLLECTION_NAME)
            .document(id)
            .get()
            .get()

        return if (doc.exists()) {
            doc.toObject(Event::class.java)
        } else {
            null
        }
    }

    /**
     * Creates a new event with an auto-generated document ID.
     *
     * Firestore generates a unique ID for the document. The ID is returned
     * along with the event so it can be included in API responses.
     *
     * @param event Event entity to persist
     * @return Pair of (generated document ID, saved event)
     */
    fun save(event: Event): Pair<String, Event> {
        val docRef = firestore.collection(COLLECTION_NAME).document()
        docRef.set(event).get()
        return Pair(docRef.id, event)
    }

    /**
     * Replaces an existing event document.
     *
     * Overwrites the entire document with the new event data.
     * Used for updates where all fields may change.
     *
     * @param id Document ID of the event to update
     * @param event New event data to persist
     * @return The updated event entity
     */
    fun update(id: String, event: Event): Event {
        firestore.collection(COLLECTION_NAME)
            .document(id)
            .set(event)
            .get()
        return event
    }

    /**
     * Deletes an event document.
     *
     * Note: This does not delete associated event_users records.
     * The service layer handles cascading deletes.
     *
     * @param id Document ID of the event to delete
     * @return true if deletion succeeded, false on error
     */
    fun delete(id: String): Boolean {
        return try {
            firestore.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .get()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Finds an event by its unique share code.
     *
     * Share codes (format: IH-XXXXX) allow events to be discovered without
     * knowing the document ID. Used when users enter a code to join an event.
     *
     * @param shareCode The share code to search for (e.g., "IH-ABC12")
     * @return Pair of (document ID, event) if found, null otherwise
     */
    fun findByShareCode(shareCode: String): Pair<String, Event>? {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("shareCode", shareCode)
            .limit(1)
            .get()
            .get()

        return if (query.documents.isNotEmpty()) {
            val doc = query.documents[0]
            val event = doc.toObject(Event::class.java)
            if (event != null) Pair(doc.id, event) else null
        } else {
            null
        }
    }
}
