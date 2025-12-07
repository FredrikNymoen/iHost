package no.ntnu.prog2007.ihostapi.service.impl

import com.google.firebase.auth.FirebaseAuth
import no.ntnu.prog2007.ihostapi.exception.ResourceNotFoundException
import no.ntnu.prog2007.ihostapi.model.dto.CreateUserRequest
import no.ntnu.prog2007.ihostapi.model.dto.UpdateUserRequest
import no.ntnu.prog2007.ihostapi.model.dto.UserResponse
import no.ntnu.prog2007.ihostapi.model.entity.User
import no.ntnu.prog2007.ihostapi.repository.UserRepository
import no.ntnu.prog2007.ihostapi.service.UserService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

/**
 * Service implementation for User operations
 */
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : UserService {
    private val logger = Logger.getLogger(UserServiceImpl::class.java.name)

    /**
     * Get user by UID
     * @param uid The Firebase UID of the user
     * @return UserResponse DTO or null if user not found
     */
    override fun getUserById(uid: String): UserResponse? {
        val user = userRepository.findById(uid) ?: return null
        return mapToUserResponse(user, uid)
    }

    /**
     * Create a new user profile in Firestore after Firebase Auth registration
     * @param request The user creation request containing user details
     * @return The created User entity
     * @throws ResourceNotFoundException if user doesn't exist in Firebase Auth
     * @throws IllegalArgumentException if user profile already exists
     */
    override fun createUser(request: CreateUserRequest): User {
        // Verify user exists in Firebase Auth
        val userRecord = try {
            firebaseAuth.getUser(request.uid)
        } catch (e: Exception) {
            logger.warning("User not found in Firebase Auth: ${request.uid}")
            throw ResourceNotFoundException("User not found in Firebase Auth")
        }

        // Check if profile already exists
        val existingUser = userRepository.findById(request.uid)
        if (existingUser != null) {
            logger.warning("User profile already exists for UID: ${request.uid}")
            throw IllegalArgumentException("User profile already exists")
        }

        // Create User model instance
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val timestamp = now.format(formatter)

        val user = User(
            email = request.email,
            username = request.username,
            photoUrl = request.photoUrl,
            createdAt = timestamp,
            updatedAt = timestamp,
            firstName = request.firstName,
            lastName = request.lastName
        )

        return userRepository.save(user, request.uid)
    }

    /**
     * Update user profile
     * @param uid The Firebase UID of the user
     * @param request The update request with new user details
     * @return The updated UserResponse DTO
     * @throws ResourceNotFoundException if user is not found
     */
    override fun updateUser(uid: String, request: UpdateUserRequest): UserResponse {
        val currentUser = userRepository.findById(uid)
            ?: throw ResourceNotFoundException("User not found")

        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val timestamp = now.format(formatter)

        // Update only non-null fields
        val updatedUser = currentUser.copy(
            firstName = request.firstName ?: currentUser.firstName,
            lastName = request.lastName ?: currentUser.lastName,
            photoUrl = request.photoUrl ?: currentUser.photoUrl,
            updatedAt = timestamp
        )

        val savedUser = userRepository.save(updatedUser, uid)
        return mapToUserResponse(savedUser, uid)
    }

    /**
     * Get all users in the system
     * @return List of UserResponse DTOs for all users
     */
    override fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { (uid, user) ->
            mapToUserResponse(user, uid)
        }
    }

    /**
     * Helper function to map User entity to UserResponse DTO
     */
    private fun mapToUserResponse(user: User, uid: String): UserResponse {
        return UserResponse(
            uid = uid,
            email = user.email,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            photoUrl = user.photoUrl,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }

    /**
     * Check if a username is available
     * @param username The username to check
     * @return true if username is available and meets length requirements (4-12 chars), false otherwise
     */
    override fun isUsernameAvailable(username: String): Boolean {
        if (username.length !in 4..12) {
            return false
        }
        return userRepository.findByUsername(username) == null
    }

    /**
     * Get user by username
     * @param username The username to search for
     * @return User entity or null if not found
     */
    override fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    /**
     * Check if an email is available
     * @param email The email to check
     * @return true if email is not already in use, false otherwise
     */
    override fun isEmailAvailable(email: String): Boolean {
        return userRepository.findByEmail(email) == null
    }
}
