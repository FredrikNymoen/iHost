package no.ntnu.prog2007.ihostapi.repository

import com.google.cloud.firestore.Firestore
import no.ntnu.prog2007.ihostapi.model.entity.EventUser
import org.springframework.stereotype.Repository

/**
 * Data access layer for EventUser junction entities in Firestore.
 *
 * Manages the `event_users` collection which links users to events. Each document
 * represents one user's relationship to one event, including their RSVP status
 * and role (creator vs attendee).
 *
 * **Query patterns**: Supports efficient lookups in both directions:
 * - By user: "What events is this user part of?"
 * - By event: "Who is attending this event?"
 * - By both: "What is this user's status for this specific event?"
 *
 * @property firestore Injected Firestore client for database operations
 */
@Repository
class EventUserRepository(
    private val firestore: Firestore
) {
    companion object {
        /** Firestore collection name for event-user relationship documents */
        const val COLLECTION_NAME = "event_users"
    }

    /**
     * Retrieves an event-user relationship by document ID.
     *
     * @param id Firestore document ID
     * @return EventUser entity if found, null otherwise
     */
    fun findById(id: String): EventUser? {
        val doc = firestore.collection(COLLECTION_NAME)
            .document(id)
            .get()
            .get()

        return if (doc.exists()) {
            doc.toObject(EventUser::class.java)
        } else {
            null
        }
    }

    /**
     * Creates a new event-user relationship with auto-generated ID.
     *
     * @param eventUser The relationship entity to persist
     * @return Pair of (generated document ID, saved entity)
     */
    fun save(eventUser: EventUser): Pair<String, EventUser> {
        val docRef = firestore.collection(COLLECTION_NAME).document()
        docRef.set(eventUser).get()
        return Pair(docRef.id, eventUser)
    }

    /**
     * Finds all events a user is associated with.
     *
     * Returns all relationships regardless of status (pending, accepted, etc.).
     * Used to build a user's event list with their status in each.
     *
     * @param userId Firebase UID of the user
     * @return List of (document ID, EventUser) pairs for the user
     */
    fun findByUserId(userId: String): List<Pair<String, EventUser>> {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .get()
            .get()

        return query.documents.mapNotNull { doc ->
            doc.toObject(EventUser::class.java)?.let { Pair(doc.id, it) }
        }
    }

    /**
     * Finds all users associated with an event.
     *
     * Returns all relationships including creator, accepted, pending, and declined.
     * Used to build an event's attendee list.
     *
     * @param eventId Firestore document ID of the event
     * @return List of (document ID, EventUser) pairs for the event
     */
    fun findByEventId(eventId: String): List<Pair<String, EventUser>> {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("eventId", eventId)
            .get()
            .get()

        return query.documents.mapNotNull { doc ->
            doc.toObject(EventUser::class.java)?.let { Pair(doc.id, it) }
        }
    }

    /**
     * Finds a specific user's relationship to a specific event.
     *
     * Used to check if a user is already invited/attending an event,
     * or to get their current status for display purposes.
     *
     * @param eventId Firestore document ID of the event
     * @param userId Firebase UID of the user
     * @return Pair of (document ID, EventUser) if exists, null otherwise
     */
    fun findByEventIdAndUserId(eventId: String, userId: String): Pair<String, EventUser>? {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("eventId", eventId)
            .whereEqualTo("userId", userId)
            .limit(1)
            .get()
            .get()

        return if (query.documents.isNotEmpty()) {
            val doc = query.documents[0]
            doc.toObject(EventUser::class.java)?.let { Pair(doc.id, it) }
        } else {
            null
        }
    }

    /**
     * Deletes all user relationships for an event (cascade delete).
     *
     * Uses a batch delete for efficiency. Called when an event is deleted
     * to clean up all associated invitation/attendance records.
     *
     * @param eventId Firestore document ID of the event being deleted
     * @return Number of deleted documents
     */
    fun deleteByEventId(eventId: String): Int {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("eventId", eventId)
            .get()
            .get()

        val batch = firestore.batch()
        query.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().get()

        return query.size()
    }

    /**
     * Partially updates an event-user document.
     *
     * Used to update status (e.g., PENDING → ACCEPTED) and respondedAt timestamp.
     *
     * @param id Document ID of the relationship to update
     * @param updates Map of field names to new values
     * @return true if update succeeded, false on error
     */
    fun update(id: String, updates: Map<String, Any?>): Boolean {
        return try {
            firestore.collection(COLLECTION_NAME)
                .document(id)
                .update(updates)
                .get()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Finds event attendees filtered by status.
     *
     * Used to get only accepted attendees, pending invitations, etc.
     * Status is uppercased before query to handle case variations.
     *
     * @param eventId Firestore document ID of the event
     * @param status Status to filter by (e.g., "ACCEPTED", "PENDING")
     * @return List of matching (document ID, EventUser) pairs
     */
    fun findByEventIdAndStatus(eventId: String, status: String): List<Pair<String, EventUser>> {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("eventId", eventId)
            .whereEqualTo("status", status.uppercase())
            .get()
            .get()

        return query.documents.mapNotNull { doc ->
            doc.toObject(EventUser::class.java)?.let { Pair(doc.id, it) }
        }
    }

    /**
     * Finds a user's events filtered by status.
     *
     * Used to show only accepted events, pending invitations, etc.
     * Status is uppercased before query to handle case variations.
     *
     * @param userId Firebase UID of the user
     * @param status Status to filter by (e.g., "ACCEPTED", "PENDING")
     * @return List of matching (document ID, EventUser) pairs
     */
    fun findByUserIdAndStatus(userId: String, status: String): List<Pair<String, EventUser>> {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", status.uppercase())
            .get()
            .get()

        return query.documents.mapNotNull { doc ->
            doc.toObject(EventUser::class.java)?.let { Pair(doc.id, it) }
        }
    }
}
