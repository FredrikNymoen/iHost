package no.ntnu.prog2007.ihost.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Request to create a new user
 */
data class CreateUserRequest(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    @SerializedName("photoUrl")
    val photoUrl: String? = null
)

/**
 * Request to update user profile
 */
data class UpdateUserRequest(
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("photoUrl")
    val photoUrl: String? = null,
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null
)

/**
 * Response from authentication endpoints
 */
data class AuthResponse(
    @SerializedName("uid")
    val uid: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("message")
    val message: String? = null
)
