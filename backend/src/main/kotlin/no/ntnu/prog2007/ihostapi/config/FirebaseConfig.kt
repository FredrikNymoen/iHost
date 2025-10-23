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
 * Firebase configuration
 * Initializes Firebase Admin SDK with authentication and Firestore services
 */
@Configuration
class FirebaseConfig {

    /**
     * Initialize Firebase App instance
     * Loads credentials from firebase-key.json
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

        // Initialize only if not already initialized
        return if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        } else {
            FirebaseApp.getInstance()
        }
    }

    /**
     * Expose FirebaseAuth as a Spring bean
     */
    @Bean
    fun firebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth {
        return FirebaseAuth.getInstance(firebaseApp)
    }

    /**
     * Expose Firestore as a Spring bean
     */
    @Bean
    fun firestore(firebaseApp: FirebaseApp): Firestore {
        return FirestoreClient.getFirestore(firebaseApp)
    }
}
