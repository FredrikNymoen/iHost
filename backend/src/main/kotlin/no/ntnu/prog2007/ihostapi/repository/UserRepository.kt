package no.ntnu.prog2007.ihostapi.repository

import com.google.cloud.firestore.Firestore
import no.ntnu.prog2007.ihostapi.model.entity.User
import org.springframework.stereotype.Repository

/**
 * Repository for User entities in Firestore
 */
@Repository
class UserRepository(
    private val firestore: Firestore
) {
    companion object {
        const val COLLECTION_NAME = "users"
    }

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

    fun save(user: User): User {
        firestore.collection(COLLECTION_NAME)
            .document(user.uid)
            .set(user)
            .get()
        return user
    }

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
}
