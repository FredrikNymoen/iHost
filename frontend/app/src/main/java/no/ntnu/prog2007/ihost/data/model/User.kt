package no.ntnu.prog2007.ihost.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("uid")
    val uid: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("displayName")
    val displayName: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,

    @SerializedName("photoUrl")
    val photoUrl: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null, // ISO 8601 format

    @SerializedName("updatedAt")
    val updatedAt: String? = null, // ISO 8601 format

    @SerializedName("isEmailVerified")
    val isEmailVerified: Boolean = false
)

data class CreateUserRequest(
    val uid: String,
    val email: String,
    val displayName: String,
    val phoneNumber: String? = null,
    val photoUrl: String? = null
)

data class AuthResponse(
    @SerializedName("uid")
    val uid: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("displayName")
    val displayName: String? = null,

    @SerializedName("message")
    val message: String? = null
)
