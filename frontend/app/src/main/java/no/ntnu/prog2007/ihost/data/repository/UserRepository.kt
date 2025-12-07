package no.ntnu.prog2007.ihost.data.repository

import android.util.Log
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.data.model.dto.UpdateUserRequest
import no.ntnu.prog2007.ihost.data.model.dto.UserResponse
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient
import no.ntnu.prog2007.ihost.data.remote.api.UserApi

/**
 * Repository for user profile operations
 *
 * Handles user profile data operations including fetching user details,
 * checking username/email availability, and updating profiles.
 * Maps DTOs to domain models and provides Result-based error handling.
 *
 * Note: For Firebase authentication operations (login, register, logout),
 * use AuthRepository instead.
 *
 * @property userApi The Retrofit API interface for user endpoints
 */
class UserRepository(
    private val userApi: UserApi = RetrofitClient.userApi
) {

    /**
     * Get all users in the application
     *
     * Fetches all registered users. Typically used for the "Add Friend"
     * feature to display users that can be added as friends.
     *
     * @return Result containing list of all users, or error
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val usersDto = userApi.getAllUsers()
            val users = usersDto.map { mapToUser(it) }
            Log.d("UserRepository", "Loaded ${users.size} users")
            Result.success(users)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error loading users", e)
            Result.failure(e)
        }
    }

    /**
     * Get user by Firebase UID
     *
     * Fetches detailed user profile information for a specific user.
     * Used to display user details in friend lists, event attendees, etc.
     *
     * @param uid The Firebase UID of the user
     * @return Result containing user profile, or error
     */
    suspend fun getUserByUid(uid: String): Result<User> {
        return try {
            val userDto = userApi.getUserByUid(uid)
            val user = mapToUser(userDto)
            Log.d("UserRepository", "Loaded user: ${user.username}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error loading user with UID: $uid", e)
            Result.failure(e)
        }
    }

    /**
     * Check if username is available
     *
     * Validates if a username is available for registration.
     * Used during signup to ensure username uniqueness.
     *
     * @param username The username to check
     * @return Result containing true if available, false if taken, or error
     */
    suspend fun isUsernameAvailable(username: String): Result<Boolean> {
        return try {
            val response = userApi.isUsernameAvailable(username)
            val available = response["available"] == true
            Log.d("UserRepository", "Username '$username' available: $available")
            Result.success(available)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking username availability", e)
            Result.failure(e)
        }
    }

    /**
     * Check if email is available
     *
     * Validates if an email is available for registration.
     * Used during signup to ensure email uniqueness.
     *
     * @param email The email address to check
     * @return Result containing true if available, false if taken, or error
     */
    suspend fun isEmailAvailable(email: String): Result<Boolean> {
        return try {
            val response = userApi.isEmailAvailable(email)
            val available = response["available"] == true
            Log.d("UserRepository", "Email '$email' available: $available")
            Result.success(available)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking email availability", e)
            Result.failure(e)
        }
    }

    /**
     * Update user profile information
     *
     * Updates one or more profile fields for a user. All parameters
     * are optional - only provided values will be updated.
     *
     * @param uid The Firebase UID of the user to update
     * @param firstName New first name (optional)
     * @param lastName New last name (optional)
     * @param photoUrl New profile photo URL (optional)
     * @return Result containing updated user profile, or error
     */
    suspend fun updateUserProfile(
        uid: String,
        firstName: String? = null,
        lastName: String? = null,
        photoUrl: String? = null
    ): Result<User> {
        return try {
            val updateRequest = UpdateUserRequest(
                firstName = firstName,
                lastName = lastName,
                photoUrl = photoUrl
            )
            val userDto = userApi.updateUserProfile(uid, updateRequest)
            val updatedUser = mapToUser(userDto)
            Log.d("UserRepository", "User profile updated for UID: $uid")
            Result.success(updatedUser)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user profile", e)
            Result.failure(e)
        }
    }

    private fun mapToUser(dto: UserResponse): User {
        return User(
            uid = dto.uid,
            email = dto.email,
            username = dto.username,
            firstName = dto.firstName,
            lastName = dto.lastName,
            photoUrl = dto.photoUrl,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }
}
