package no.ntnu.prog2007.ihostapi.controller

import jakarta.validation.Valid
import no.ntnu.prog2007.ihostapi.exception.ForbiddenException
import no.ntnu.prog2007.ihostapi.exception.UnauthorizedException
import no.ntnu.prog2007.ihostapi.model.dto.AuthResponse
import no.ntnu.prog2007.ihostapi.model.dto.CreateUserRequest
import no.ntnu.prog2007.ihostapi.model.dto.UpdateUserRequest
import no.ntnu.prog2007.ihostapi.model.dto.UserResponse
import no.ntnu.prog2007.ihostapi.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

/**
 * REST controller for user management operations.
 *
 * Handles user profile operations including:
 * - User registration (creating Firestore profile after Firebase Auth signup)
 * - Profile retrieval and updates
 * - Username and email availability checks
 *
 *
 * @property userService Business logic service for user operations
 * @see no.ntnu.prog2007.ihostapi.service.UserService for business logic
 * @see no.ntnu.prog2007.ihostapi.model.entity.User for user data model
 */
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    private val logger = Logger.getLogger(UserController::class.java.name)


    /**
     * Retrieves all user profiles.
     *
     * Used primarily for the "invite users to event" feature where the current
     * user needs to browse and select other users to invite. Does not include
     * sensitive data like email addresses in the response.
     *
     * @return List of all users with basic profile information
     */
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val uid = getCurrentUserId() // Verify authentication
        val users = userService.getAllUsers()

        logger.info("Retrieved ${users.size} users for user: $uid")
        return ResponseEntity.ok(users)
    }

    /**
     * Retrieves a specific user's profile by Firebase UID.
     *
     * Returns public profile information for any user. This is used when
     * displaying user details in event participant lists, friend lists, etc.
     *
     * @param uid The Firebase UID of the user to retrieve
     * @return User profile information
     * @throws IllegalArgumentException if user with the given UID doesn't exist
     */
    @GetMapping("/{uid}")
    fun getUserByUid(@PathVariable uid: String): ResponseEntity<UserResponse> {
        val user = userService.getUserById(uid)
            ?: throw IllegalArgumentException("User not found")

        logger.info("Retrieved user information for UID: $uid")
        return ResponseEntity.ok(user)
    }

    /**
     * Creates a user profile in Firestore after Firebase Auth registration.
     *
     * This is a public endpoint (no authentication required) called immediately after
     * the mobile app creates a Firebase Auth account. It creates the corresponding
     * user document in Firestore with profile details.
     *
     * The request must include the Firebase UID obtained from Firebase Auth signup.
     * Username and email uniqueness are validated before creation.
     *
     * @param request User profile data including UID, email, username, and display name
     * @return Success response with user details (HTTP 201)
     * @throws IllegalArgumentException if username/email already exists
     */
    @PostMapping("/register")
    fun registerUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<AuthResponse> {
        val user = userService.createUser(request)

        logger.info("User profile created in Firestore for UID: ${request.uid}")

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                AuthResponse(
                    uid = request.uid,
                    email = user.email,
                    username = user.username,
                    message = "Brukerprofil opprettet. Du kan nå logge inn."
                )
            )
    }

    /**
     * Updates a user's profile information.
     *
     * Users can only update their own profile. The service enforces this by
     * comparing the path UID with the authenticated user's UID. Partial updates
     * are supported (only provided fields are updated).
     *
     * @param uid The Firebase UID of the user to update (must match authenticated user)
     * @param request Updated profile data (username, displayName, bio, etc.)
     * @return Updated user profile
     * @throws ForbiddenException if user attempts to update someone else's profile
     */
    @PutMapping("/{uid}")
    fun updateUserProfile(
        @PathVariable uid: String,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val currentUserId = getCurrentUserId()

        // Verify user is updating their own profile
        if (currentUserId != uid) {
            throw ForbiddenException("You can only update your own profile")
        }

        val updatedUser = userService.updateUser(uid, request)

        logger.info("User profile updated: $uid")
        return ResponseEntity.ok(updatedUser)
    }

    /**
     * Checks if a username is available for registration.
     *
     * This is a public endpoint used during signup to validate usernames
     * in real-time as the user types. Returns true if the username is
     * not already taken by another user.
     *
     * @param username The username to check
     * @return Map with "available" boolean indicating if username is free
     */
    @GetMapping("/username-available/{username}")
    fun isUsernameAvailable(@PathVariable username: String): ResponseEntity<Map<String, Boolean>> {
        val available = userService.isUsernameAvailable(username)
        return ResponseEntity.ok(mapOf("available" to available))
    }

    /**
     * Checks if an email address is available for registration.
     *
     * This is a public endpoint used during signup to validate email addresses
     * in real-time. Returns true if the email is not already registered in
     * Firestore (note: Firebase Auth has its own email uniqueness check).
     *
     * @param email The email address to check
     * @return Map with "available" boolean indicating if email is free
     */
    @GetMapping("/email-available/{email}")
    fun isEmailAvailable(@PathVariable email: String): ResponseEntity<Map<String, Boolean>> {
        logger.info { "Checking email availability for: $email" }
        val available = userService.isEmailAvailable(email)
        return ResponseEntity.ok(mapOf("available" to available))
    }

    /**
     * Extracts the Firebase UID from the SecurityContext.
     *
     * The UID is placed in the SecurityContext by [FirebaseTokenFilter]
     * after successfully validating the JWT token.
     *
     * @return Firebase UID of the authenticated user
     * @throws UnauthorizedException if no valid authentication exists
     * @see no.ntnu.prog2007.ihostapi.security.filter.FirebaseTokenFilter
     */
    private fun getCurrentUserId(): String {
        return SecurityContextHolder.getContext().authentication.principal as? String
            ?: throw UnauthorizedException("Token is invalid or missing")
    }
}
