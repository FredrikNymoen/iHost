package no.ntnu.prog2007.ihostapi.repository

import com.google.cloud.firestore.Firestore
import no.ntnu.prog2007.ihostapi.model.entity.Friendship
import org.springframework.stereotype.Repository

/**
 * Repository for Friendship entities in Firestore
 */
@Repository
class FriendshipRepository(
    private val firestore: Firestore
) {
    companion object {
        const val COLLECTION_NAME = "friendships"
    }

    fun findById(id: String): Friendship? {
        val doc = firestore.collection(COLLECTION_NAME)
            .document(id)
            .get()
            .get()

        return if (doc.exists()) {
            doc.toObject(Friendship::class.java)
        } else {
            null
        }
    }

    fun save(friendship: Friendship): Friendship {
        firestore.collection(COLLECTION_NAME)
            .document(friendship.id)
            .set(friendship)
            .get()
        return friendship
    }

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

    fun findByUserId(userId: String): List<Friendship> {
        val query1 = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("user1Id", userId)
            .get()
            .get()

        val query2 = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("user2Id", userId)
            .get()
            .get()

        val friendships = mutableListOf<Friendship>()

        query1.documents.mapNotNullTo(friendships) { doc ->
            doc.toObject(Friendship::class.java)
        }

        query2.documents.mapNotNullTo(friendships) { doc ->
            doc.toObject(Friendship::class.java)
        }

        return friendships
    }
}
