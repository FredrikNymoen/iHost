package no.ntnu.prog2007.ihostapi.repository

import com.google.cloud.firestore.Firestore
import no.ntnu.prog2007.ihostapi.model.entity.User
import org.springframework.stereotype.Repository

/**
 * Data access layer for User entities in Firestore.
 *
 * Manages CRUD operations on the `users` collection. Document IDs correspond
 * to Firebase Auth UIDs, creating a direct link between authentication and profile data.
 *
 * **Blocking operations**: All Firestore calls use `.get()` to block until completion.
 * This simplifies the service layer but means requests are synchronous.
 *
 * @property firestore Injected Firestore client for database operations
 */
@Repository
class UserRepository(
    private val firestore: Firestore
) {
    companion object {
        /** Firestore collection name for user documents */
        const val COLLECTION_NAME = "users"
    }

    /**
     * Retrieves a user by their Firebase Auth UID.
     *
     * @param uid Firebase Auth user ID (also the Firestore document ID)
     * @return User entity if found, null otherwise
     */
    fun findById(uid: String): User? {
        val doc = firestore.collection(COLLECTION_NAME)
            .document(uid)
            .get()
            .get()

        return if (doc.exists()) {
            doc.toObject(User::class.java)
        } else {
            null
        }
    }

    /**
     * Creates or overwrites a user document.
     *
     * Uses the Firebase UID as the document ID to maintain the auth-profile link.
     * Overwrites any existing document with the same UID.
     *
     * @param user User entity to persist
     * @param uid Firebase Auth UID to use as document ID
     * @return The saved user entity
     */
    fun save(user: User, uid: String): User {
        firestore.collection(COLLECTION_NAME)
            .document(uid)
            .set(user)
            .get()
        return user
    }

    /**
     * Partially updates a user document.
     *
     * Only the fields present in the updates map are modified.
     * Null values in the map will set those fields to null in Firestore.
     *
     * @param uid Firebase Auth UID of the user to update
     * @param updates Map of field names to new values
     * @return true if update succeeded, false on error
     */
    fun update(uid: String, updates: Map<String, Any?>): Boolean {
        return try {
            firestore.collection(COLLECTION_NAME)
                .document(uid)
                .update(updates)
                .get()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Deletes a user document.
     *
     * Note: This only removes the profile data. Firebase Auth user must be
     * deleted separately if full account removal is needed.
     *
     * @param uid Firebase Auth UID of the user to delete
     * @return true if deletion succeeded, false on error
     */
    fun delete(uid: String): Boolean {
        return try {
            firestore.collection(COLLECTION_NAME)
                .document(uid)
                .delete()
                .get()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Finds a user by their unique username.
     *
     * Used for username availability checks during registration
     * and for looking up users by their display name.
     *
     * @param username The username to search for (case-sensitive)
     * @return User if found, null otherwise
     */
    fun findByUsername(username: String): User? {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .get()

        return if (query.documents.isNotEmpty()) {
            query.documents[0].toObject(User::class.java)
        } else {
            null
        }
    }

    /**
     * Finds a user by their email address.
     *
     * Used for email availability checks. Email should match
     * the Firebase Auth email for consistency.
     *
     * @param email Email address to search for (case-sensitive)
     * @return User if found, null otherwise
     */
    fun findByEmail(email: String): User? {
        val query = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .get()

        return if (query.documents.isNotEmpty()) {
            query.documents[0].toObject(User::class.java)
        } else {
            null
        }
    }

    /**
     * Retrieves all users in the system.
     *
     * Returns pairs of (documentId, User) so callers have access to the UID.
     * Used for admin/listing purposes and friend discovery features.
     *
     * @return List of all users with their document IDs
     */
    fun findAll(): List<Pair<String, User>> {
        val query = firestore.collection(COLLECTION_NAME)
            .get()
            .get()

        return query.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.let { Pair(doc.id, it) }
        }
    }
}
