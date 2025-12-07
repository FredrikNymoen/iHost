package no.ntnu.prog2007.ihostapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main entry point for the iHost API backend application.
 *
 * This Spring Boot application serves as the REST API backend for the iHost mobile application,
 * providing endpoints for user management, event creation/management, friendships, and image uploads.
 *
 * The application uses a layered architecture:
 * - **Controllers**: Handle HTTP requests and route them to appropriate services
 * - **Services**: Contain business logic and orchestrate data operations
 * - **Repositories**: Abstract Firestore database operations
 *
 * Key integrations:
 * - **Firebase Auth**: JWT-based authentication for secure API access
 * - **Firestore**: NoSQL database for storing users, events, and relationships
 * - **Cloudinary**: Cloud-based image storage and optimization
 *
 * @see no.ntnu.prog2007.ihostapi.config.SecurityConfig for authentication configuration
 * @see no.ntnu.prog2007.ihostapi.config.FirebaseConfig for Firebase/Firestore setup
 */
@SpringBootApplication
class IHostApiApplication

/**
 * Application bootstrap function.
 *
 * Initializes the Spring application context, triggering component scanning,
 * auto-configuration, and bean creation for all application components.
 *
 * @param args Command-line arguments passed to the application
 */
fun main(args: Array<String>) {
    runApplication<IHostApiApplication>(*args)
}




