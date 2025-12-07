package no.ntnu.prog2007.ihostapi.service

import no.ntnu.prog2007.ihostapi.model.dto.CreateUserRequest
import no.ntnu.prog2007.ihostapi.model.dto.UpdateUserRequest
import no.ntnu.prog2007.ihostapi.model.dto.UserResponse
import no.ntnu.prog2007.ihostapi.model.entity.User

/**
 * Service interface for User operations
 */
interface UserService {
    /**
     * Get user by UID
     * @param uid The Firebase UID of the user
     * @return UserResponse DTO or null if user not found
     */
    fun getUserById(uid: String): UserResponse?

    /**
     * Create a new user profile in Firestore after Firebase Auth registration
     * @param request The user creation request containing user details
     * @return The created User entity
     * @throws ResourceNotFoundException if user doesn't exist in Firebase Auth
     * @throws IllegalArgumentException if user profile already exists
     */
    fun createUser(request: CreateUserRequest): User

    /**
     * Update user profile
     * @param uid The Firebase UID of the user
     * @param request The update request with new user details
     * @return The updated UserResponse DTO
     * @throws ResourceNotFoundException if user is not found
     */
    fun updateUser(uid: String, request: UpdateUserRequest): UserResponse

    /**
     * Get all users in the system
     * @return List of UserResponse DTOs for all users
     */
    fun getAllUsers(): List<UserResponse>

    /**
     * Check if a username is available
     * @param username The username to check
     * @return true if username is available and meets length requirements (4-12 chars), false otherwise
     */
    fun isUsernameAvailable(username: String): Boolean

    /**
     * Get user by username
     * @param username The username to search for
     * @return User entity or null if not found
     */
    fun getUserByUsername(username: String): User?

    /**
     * Check if an email is available
     * @param email The email to check
     * @return true if email is not already in use, false otherwise
     */
    fun isEmailAvailable(email: String): Boolean
}
