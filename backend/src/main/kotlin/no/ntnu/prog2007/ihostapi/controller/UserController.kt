package no.ntnu.prog2007.ihostapi.controller

import jakarta.validation.Valid
import no.ntnu.prog2007.ihostapi.exception.ForbiddenException
import no.ntnu.prog2007.ihostapi.exception.UnauthorizedException
import no.ntnu.prog2007.ihostapi.model.dto.AuthResponse
import no.ntnu.prog2007.ihostapi.model.dto.CreateUserRequest
import no.ntnu.prog2007.ihostapi.model.dto.UpdateUserRequest
import no.ntnu.prog2007.ihostapi.model.entity.User
import no.ntnu.prog2007.ihostapi.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    private val logger = Logger.getLogger(UserController::class.java.name)


    /**
     * Get all users (for inviting users to events)
     */
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<User>> {
        val uid = getCurrentUserId() // Verify authentication
        val users = userService.getAllUsers()

        logger.info("Retrieved ${users.size} users for user: $uid")
        return ResponseEntity.ok(users)
    }

    /**
     * Get user by UID (public endpoint)
     */
    @GetMapping("/{uid}")
    fun getUserByUid(@PathVariable uid: String): ResponseEntity<User> {
        val user = userService.getUserById(uid)
            ?: throw IllegalArgumentException("User not found")

        logger.info("Retrieved user information for UID: $uid")
        return ResponseEntity.ok(user)
    }

    /**
     * Create user profile in Firestore after Firebase Auth registration
     */
    @PostMapping("/register")
    fun registerUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<AuthResponse> {
        val user = userService.createUser(request)

        logger.info("User profile created in Firestore for UID: ${user.uid}")

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                AuthResponse(
                    uid = user.uid,
                    email = user.email,
                    username = user.username,
                    message = "Brukerprofil opprettet. Du kan nå logge inn."
                )
            )
    }

    /**
     * Update user profile (only the user can update their own profile)
     */
    @PutMapping("/{uid}")
    fun updateUserProfile(
        @PathVariable uid: String,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<User> {
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
     * Check if a username is available
     */
    @GetMapping("/username-available/{username}")
    fun isUsernameAvailable(@PathVariable username: String): ResponseEntity<Map<String, Boolean>> {
        val available = userService.isUsernameAvailable(username)
        return ResponseEntity.ok(mapOf("available" to available))
    }

    /**
     * Helper function to get current authenticated user ID
     */
    private fun getCurrentUserId(): String {
        return SecurityContextHolder.getContext().authentication.principal as? String
            ?: throw UnauthorizedException("Token is invalid or missing")
    }
}
