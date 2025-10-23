package no.ntnu.prog2007.ihost.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

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
    val createdAt: LocalDateTime? = null,

    @SerializedName("updatedAt")
    val updatedAt: LocalDateTime? = null,

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
