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
            doc.toObject(Friendship::class.java)?.copy(id = doc.id)
        } else {
            null
        }
    }

    fun save(user1Id: String, user2Id: String, status: String, requestedBy: String, requestedAt: String, respondedAt: String?): Friendship {
        val friendshipData = mapOf(
            "user1Id" to user1Id,
            "user2Id" to user2Id,
            "status" to status,
            "requestedBy" to requestedBy,
            "requestedAt" to requestedAt,
            "respondedAt" to respondedAt
        )

        val docRef = firestore.collection(COLLECTION_NAME)
            .add(friendshipData)
            .get()

        return Friendship(
            id = docRef.id,
            user1Id = user1Id,
            user2Id = user2Id,
            status = status,
            requestedBy = requestedBy,
            requestedAt = requestedAt,
            respondedAt = respondedAt
        )
    }

    fun update(friendship: Friendship): Boolean {
        return try {
            val friendshipData = mapOf(
                "user1Id" to friendship.user1Id,
                "user2Id" to friendship.user2Id,
                "status" to friendship.status,
                "requestedBy" to friendship.requestedBy,
                "requestedAt" to friendship.requestedAt,
                "respondedAt" to friendship.respondedAt
            )

            firestore.collection(COLLECTION_NAME)
                .document(friendship.id)
                .set(friendshipData)
                .get()
            true
        } catch (e: Exception) {
            false
        }
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

    fun findByUsers(userId1: String, userId2: String): Friendship? {
        // Check user1 -> user2
        val query1 = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("user1Id", userId1)
            .whereEqualTo("user2Id", userId2)
            .limit(1)
            .get()
            .get()

        if (!query1.isEmpty) {
            val doc = query1.documents.first()
            return doc.toObject(Friendship::class.java)?.copy(id = doc.id)
        }

        // Check user2 -> user1
        val query2 = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("user1Id", userId2)
            .whereEqualTo("user2Id", userId1)
            .limit(1)
            .get()
            .get()

        if (!query2.isEmpty) {
            val doc = query2.documents.first()
            return doc.toObject(Friendship::class.java)?.copy(id = doc.id)
        }

        return null
    }

    fun findPendingRequestsForUser(userId: String): List<Friendship> {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("user2Id", userId)
            .whereEqualTo("status", "PENDING")
            .get()
            .get()

        return query.documents.mapNotNull { doc ->
            doc.toObject(Friendship::class.java)?.copy(id = doc.id)
        }
    }

    fun findAcceptedFriendshipsForUser(userId: String): List<Friendship> {
        val query1 = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("user1Id", userId)
            .whereEqualTo("status", "ACCEPTED")
            .get()
            .get()

        val friendships1 = query1.documents.mapNotNull { doc ->
            doc.toObject(Friendship::class.java)?.copy(id = doc.id)
        }

        val query2 = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("user2Id", userId)
            .whereEqualTo("status", "ACCEPTED")
            .get()
            .get()

        val friendships2 = query2.documents.mapNotNull { doc ->
            doc.toObject(Friendship::class.java)?.copy(id = doc.id)
        }

        return friendships1 + friendships2
    }

    fun findSentRequestsByUser(userId: String): List<Friendship> {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("user1Id", userId)
            .whereEqualTo("status", "PENDING")
            .get()
            .get()

        return query.documents.mapNotNull { doc ->
            doc.toObject(Friendship::class.java)?.copy(id = doc.id)
        }
    }
}
