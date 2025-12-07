package no.ntnu.prog2007.ihostapi.repository

import com.google.cloud.firestore.Firestore
import no.ntnu.prog2007.ihostapi.model.entity.Friendship
import org.springframework.stereotype.Repository

/**
 * Data access layer for Friendship entities in Firestore.
 *
 * Manages the `friendships` collection representing social connections between users.
 * Each document stores a bidirectional relationship, where user1Id is always the
 * requester and user2Id is the recipient.
 *
 * **Bidirectional queries**: Since friendships are stored once (not duplicated),
 * queries for a user's friends must check both user1Id and user2Id fields.
 * The repository handles this complexity internally.
 *
 * **ID copying**: Firestore document IDs are copied into the returned entities
 * using `.copy(id = doc.id)` since the ID is needed for update/delete operations.
 *
 * @property firestore Injected Firestore client for database operations
 */
@Repository
class FriendshipRepository(
    private val firestore: Firestore
) {
    companion object {
        /** Firestore collection name for friendship documents */
        const val COLLECTION_NAME = "friendships"
    }

    /**
     * Retrieves a friendship by document ID.
     *
     * @param id Firestore document ID
     * @return Friendship entity with ID populated, or null if not found
     */
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

    /**
     * Creates a new friendship/friend request.
     *
     * By convention, user1Id is the sender and user2Id is the recipient.
     * Uses Firestore's add() for auto-generated document IDs.
     *
     * @param user1Id Firebase UID of the request sender
     * @param user2Id Firebase UID of the request recipient
     * @param status Initial status (typically "PENDING")
     * @param requestedBy UID of who initiated (same as user1Id for new requests)
     * @param requestedAt ISO-8601 timestamp of request creation
     * @param respondedAt Null for new requests, set when accepted/declined
     * @return Created Friendship entity with generated document ID
     */
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

    /**
     * Updates an existing friendship document.
     *
     * Replaces the entire document with the new friendship data.
     * Used to change status from PENDING to ACCEPTED/DECLINED.
     *
     * @param friendship Updated friendship entity (must have valid ID)
     * @return true if update succeeded, false on error
     */
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

    /**
     * Deletes a friendship document.
     *
     * Used when users unfriend each other or cancel pending requests.
     *
     * @param id Document ID of the friendship to delete
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
     * Finds any existing friendship between two users.
     *
     * Checks both directions since the relationship could be stored as
     * (user1→user2) or (user2→user1). Used to prevent duplicate requests.
     *
     * @param userId1 Firebase UID of one user
     * @param userId2 Firebase UID of the other user
     * @return Existing Friendship if found, null otherwise
     */
    fun findByUsers(userId1: String, userId2: String): Friendship? {
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

    /**
     * Finds pending friend requests sent TO a user.
     *
     * Only returns requests where the user is the recipient (user2Id)
     * and status is PENDING. Used to show incoming friend requests.
     *
     * @param userId Firebase UID of the recipient
     * @return List of pending incoming friend requests
     */
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

    /**
     * Finds all accepted friendships for a user.
     *
     * Must query both user1Id and user2Id since the user could be
     * on either side of the relationship. Combines both result sets.
     *
     * @param userId Firebase UID of the user
     * @return List of all accepted friendships involving the user
     */
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

    /**
     * Finds pending friend requests sent BY a user.
     *
     * Only returns requests where the user is the sender (user1Id)
     * and status is PENDING. Used to show outgoing pending requests.
     *
     * @param userId Firebase UID of the sender
     * @return List of pending outgoing friend requests
     */
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
