package no.ntnu.prog2007.ihostapi.repository

import com.google.cloud.firestore.Firestore
import no.ntnu.prog2007.ihostapi.model.entity.Event
import org.springframework.stereotype.Repository

/**
 * Repository for Event entities in Firestore
 */
@Repository
class EventRepository(
    private val firestore: Firestore
) {
    companion object {
        const val COLLECTION_NAME = "events"
    }

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

    fun save(event: Event): Pair<String, Event> {
        val docRef = firestore.collection(COLLECTION_NAME).document()
        docRef.set(event).get()
        return Pair(docRef.id, event)
    }

    fun update(id: String, event: Event): Event {
        firestore.collection(COLLECTION_NAME)
            .document(id)
            .set(event)
            .get()
        return event
    }

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
