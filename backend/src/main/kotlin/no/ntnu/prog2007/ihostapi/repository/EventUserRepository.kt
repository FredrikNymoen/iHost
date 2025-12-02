package no.ntnu.prog2007.ihostapi.repository

import com.google.cloud.firestore.Firestore
import no.ntnu.prog2007.ihostapi.model.entity.EventUser
import org.springframework.stereotype.Repository

/**
 * Repository for EventUser entities in Firestore
 */
@Repository
class EventUserRepository(
    private val firestore: Firestore
) {
    companion object {
        const val COLLECTION_NAME = "event_users"
    }

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

    fun save(eventUser: EventUser): Pair<String, EventUser> {
        val docRef = firestore.collection(COLLECTION_NAME).document()
        docRef.set(eventUser).get()
        return Pair(docRef.id, eventUser)
    }

    fun findByUserId(userId: String): List<Pair<String, EventUser>> {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .get()
            .get()

        return query.documents.mapNotNull { doc ->
            doc.toObject(EventUser::class.java)?.let { Pair(doc.id, it) }
        }
    }

    fun findByEventId(eventId: String): List<Pair<String, EventUser>> {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("eventId", eventId)
            .get()
            .get()

        return query.documents.mapNotNull { doc ->
            doc.toObject(EventUser::class.java)?.let { Pair(doc.id, it) }
        }
    }

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
}
