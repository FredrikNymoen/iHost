package no.ntnu.prog2007.ihost.data.model.domain

import com.google.gson.annotations.SerializedName

/**
 * User domain model
 */
data class User(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    @SerializedName("photoUrl")
    val photoUrl: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("isEmailVerified")
    val isEmailVerified: Boolean = false
)
