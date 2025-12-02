package no.ntnu.prog2007.ihost.data.repository

import android.util.Log
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.data.model.dto.UpdateUserRequest
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient
import no.ntnu.prog2007.ihost.data.remote.api.UserApi

class UserRepository(
    private val userApi: UserApi = RetrofitClient.userApi
) {

    /**
     * Get all users
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val users = userApi.getAllUsers()
            Log.d("UserRepository", "Loaded ${users.size} users")
            Result.success(users)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error loading users", e)
            Result.failure(e)
        }
    }

    /**
     * Get user by UID
     */
    suspend fun getUserByUid(uid: String): Result<User> {
        return try {
            val user = userApi.getUserByUid(uid)
            Log.d("UserRepository", "Loaded user: ${user.username}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error loading user with UID: $uid", e)
            Result.failure(e)
        }
    }

    /**
     * Check if username is available
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
     * Update user profile
     */
    suspend fun updateUserProfile(
        uid: String,
        firstName: String? = null,
        lastName: String? = null,
        photoUrl: String? = null,
        phoneNumber: String? = null
    ): Result<User> {
        return try {
            val updateRequest = UpdateUserRequest(
                firstName = firstName,
                lastName = lastName,
                photoUrl = photoUrl,
                phoneNumber = phoneNumber
            )
            val updatedUser = userApi.updateUserProfile(uid, updateRequest)
            Log.d("UserRepository", "User profile updated for UID: $uid")
            Result.success(updatedUser)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user profile", e)
            Result.failure(e)
        }
    }
}
