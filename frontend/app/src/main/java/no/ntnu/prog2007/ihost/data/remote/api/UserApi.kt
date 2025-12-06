package no.ntnu.prog2007.ihost.data.remote.api

import no.ntnu.prog2007.ihost.data.model.dto.AuthResponse
import no.ntnu.prog2007.ihost.data.model.dto.CreateUserRequest
import no.ntnu.prog2007.ihost.data.model.dto.UpdateUserRequest
import no.ntnu.prog2007.ihost.data.model.dto.UserResponse
import no.ntnu.prog2007.ihost.data.remote.config.ApiEndpoints.USERS
import retrofit2.http.*

/**
 * Retrofit API interface for user-related endpoints
 *
 * Handles user profile operations, registration validation, and profile updates.
 * Most endpoints require Firebase authentication (except public endpoints).
 */
interface UserApi {
    /**
     * Register a new user in the backend database
     *
     * Called after Firebase Auth registration to create the user profile.
     * Requires valid Firebase token in Authorization header.
     *
     * @param request User registration details (uid, username, email, etc.)
     * @return Authentication response with success message
     */
    @POST("$USERS/register")
    suspend fun registerUser(
        @Body request: CreateUserRequest
    ): AuthResponse

    /**
     * Get all registered users
     *
     * Returns all users in the system. Used for friend discovery.
     *
     * @return List of all user profiles
     */
    @GET(USERS)
    suspend fun getAllUsers(): List<UserResponse>

    /**
     * Get user profile by Firebase UID
     *
     * @param uid The Firebase UID
     * @return User profile data
     */
    @GET("$USERS/{uid}")
    suspend fun getUserByUid(
        @Path("uid") uid: String
    ): UserResponse

    /**
     * Check if username is available for registration
     *
     * Public endpoint - no authentication required.
     *
     * @param username The username to check
     * @return Map with "available" boolean field
     */
    @GET("$USERS/username-available/{username}")
    suspend fun isUsernameAvailable(
        @Path("username") username: String
    ): Map<String, Boolean>

    /**
     * Check if email is available for registration
     *
     * Public endpoint - no authentication required.
     *
     * @param email The email address to check (URL encoded)
     * @return Map with "available" boolean field
     */
    @GET("$USERS/email-available/{email}")
    suspend fun isEmailAvailable(
        @Path("email", encoded = false) email: String
    ): Map<String, Boolean>

    /**
     * Update user profile information
     *
     * Updates one or more profile fields. Only provided fields are updated.
     *
     * @param uid The Firebase UID of the user
     * @param request Update request with new profile data
     * @return Updated user profile
     */
    @PUT("$USERS/{uid}")
    suspend fun updateUserProfile(
        @Path("uid") uid: String,
        @Body request: UpdateUserRequest
    ): UserResponse
}
