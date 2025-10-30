package no.ntnu.prog2007.ihostapi.controller

import com.google.cloud.firestore.Firestore
import com.google.firebase.auth.FirebaseAuth
import jakarta.validation.Valid
import no.ntnu.prog2007.ihostapi.model.AuthResponse
import no.ntnu.prog2007.ihostapi.model.CreateUserRequest
import no.ntnu.prog2007.ihostapi.model.ErrorResponse
import no.ntnu.prog2007.ihostapi.model.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val firestore: Firestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val logger = Logger.getLogger(AuthController::class.java.name)

    companion object {
        // Firestore collection name for users
        const val USERS_COLLECTION = "users"
    }

    /**
     * Verify that the user is authenticated and return user data
     * Requires valid Firebase JWT token in Authorization header
     *
     * BRUKES IKKE AKKURAT NÅ, KANSKJE FJERNES?
     */
    @GetMapping("/verify")
    fun verifyToken(): ResponseEntity<Any> {
        return try {
            // Get authenticated user UID from security context
            val uid = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            // Fetch user document from Firestore
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .get()

            if (!userDoc.exists()) {
                logger.warning("User document not found in Firestore for UID: $uid")
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "Bruker ikke funnet"))
            }

            // Convert Firestore document to User model
            val user = userDoc.toObject(User::class.java)

            if (user != null) {
                logger.info("Token verified for user: $uid")
                ResponseEntity.ok(user)
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse("ERROR", "Could not parse user data"))
            }
        } catch (e: Exception) {
            logger.warning("Error verifying token: ${e.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse("UNAUTHORIZED", "Token verification failed"))
        }
    }

    /**
     * Get user by UID
     * Used to retrieve user information (like display name) for a specific UID
     * Public endpoint - does not require authentication
     */
    @GetMapping("/user/{uid}")
    fun getUserByUid(@PathVariable uid: String): ResponseEntity<Any> {
        return try {
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .get()

            if (!userDoc.exists()) {
                logger.warning("User document not found in Firestore for UID: $uid")
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "Bruker ikke funnet"))
            }

            val user = userDoc.toObject(User::class.java)

            if (user != null) {
                logger.info("Retrieved user information for UID: $uid")
                ResponseEntity.ok(user)
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse("ERROR", "Could not parse user data"))
            }
        } catch (e: Exception) {
            logger.warning("Error retrieving user $uid: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not retrieve user"))
        }
    }

    /**
     * Create user profile in Firestore after Firebase Auth registration
     *
     * Flow:
     * 1. Android registers user in Firebase Auth → gets UID
     * 2. Android calls this endpoint with UID + profile data
     * 3. Backend creates User profile in Firestore
     *
     * Requires: UID from Firebase Auth (sent by Android)
     */
    @PostMapping("/register")
    fun registerUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<Any> {
        return try {
            val uid = request.uid

            // Verify user exists in Firebase Auth
            val userRecord = try {
                firebaseAuth.getUser(uid)
            } catch (e: Exception) {
                logger.warning("User not found in Firebase Auth: $uid")
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("USER_NOT_FOUND", "Bruker finnes ikke i Firebase Auth"))
            }

            // Check if profile already exists
            val existingDoc = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .get()

            if (existingDoc.exists()) {
                logger.warning("User profile already exists for UID: $uid")
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse("PROFILE_EXISTS", "Brukerprofil finnes allerede"))
            }

            // Create User model instance
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val timestamp = now.format(formatter)

            val user = User(
                uid = uid,
                email = request.email,
                displayName = request.displayName,
                phoneNumber = request.phoneNumber,
                photoUrl = request.photoUrl,
                createdAt = timestamp,
                updatedAt = timestamp,
                isEmailVerified = userRecord.isEmailVerified
            )

            // Save user document to Firestore and wait for completion
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .set(user)
                .get()

            logger.info("User profile created in Firestore for UID: $uid")

            // Return success response with user profile
            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    AuthResponse(
                        uid = user.uid,
                        email = user.email,
                        displayName = user.displayName,
                        message = "Brukerprofil opprettet. Du kan nå logge inn."
                    )
                )
        } catch (e: Exception) {
            logger.warning("Error creating user profile: ${e.message}")
            val errorMessage = when {
                e.message?.contains("No user record") == true -> "Bruker finnes ikke i Firebase Auth"
                e.message?.contains("already exists") == true -> "Brukerprofil finnes allerede"
                else -> e.message ?: "Ukjent feil ved opprettelse av profil"
            }

            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                    ErrorResponse(
                        error = "PROFILE_CREATION_FAILED",
                        message = errorMessage
                    )
                )
        }
    }
}