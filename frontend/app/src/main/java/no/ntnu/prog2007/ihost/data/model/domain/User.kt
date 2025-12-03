package no.ntnu.prog2007.ihost.data.model.domain

/**
 * User domain model
 */
data class User(
    val uid: String,
    val email: String,
    val username: String,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val firstName: String,
    val lastName: String? = null,
    val isEmailVerified: Boolean = false
)
