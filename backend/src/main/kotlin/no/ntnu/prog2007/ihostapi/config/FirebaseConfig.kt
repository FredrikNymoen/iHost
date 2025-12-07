package no.ntnu.prog2007.ihostapi.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.database.FirebaseDatabase
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream

/**
 * Firebase Admin SDK configuration.
 *
 * Initializes Firebase services required by the backend:
 * - **FirebaseAuth**: Verifies JWT tokens from mobile clients
 * - **Firestore**: NoSQL database for all application data
 *
 * Firebase was chosen as the backend-as-a-service platform because:
 * - Automatic scaling without infrastructure management
 * - Strong consistency guarantees for document operations
 * - Free and easy to use
 *
 *
 * @see SecurityConfig for how FirebaseAuth is used in request authentication
 */
@Configuration
class FirebaseConfig {

    /**
     * Creates and initializes the Firebase application instance.
     *
     * Uses idempotent initialization to handle Spring context refreshes
     * and hot reloads during development without causing duplicate app errors.
     *
     * @return The initialized FirebaseApp instance
     * @throws java.io.FileNotFoundException if firebase-key.json is missing (must be in root directory)
     */
    @Bean
    fun firebaseApp(): FirebaseApp {
        val serviceAccountPath = "firebase-key.json"

        val credentials = GoogleCredentials.fromStream(
            FileInputStream(serviceAccountPath)
        )

        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build()

        return if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        } else {
            FirebaseApp.getInstance()
        }
    }

    /**
     * Provides FirebaseAuth for JWT token verification.
     *
     * Used by [FirebaseTokenFilter] to validate tokens on each authenticated request.
     * The `verifyIdToken` method checks token signature, expiration, and issuer.
     *
     * @param firebaseApp The initialized Firebase application
     * @return FirebaseAuth instance for token operations
     */
    @Bean
    fun firebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth {
        return FirebaseAuth.getInstance(firebaseApp)
    }

    /**
     * Provides Firestore database client.
     *
     * All repositories use this client to perform CRUD operations on collections:
     * - `users`: User profiles linked to Firebase Auth UIDs
     * - `events`: Event data with share codes
     * - `event_users`: Junction table for event-user relationships
     * - `friendships`: User friendship connections and requests
     * - `event_images`: Image metadata with Cloudinary URLs
     *
     * @param firebaseApp The initialized Firebase application
     * @return Firestore client for database operations
     */
    @Bean
    fun firestore(firebaseApp: FirebaseApp): Firestore {
        return FirestoreClient.getFirestore(firebaseApp)
    }
}
