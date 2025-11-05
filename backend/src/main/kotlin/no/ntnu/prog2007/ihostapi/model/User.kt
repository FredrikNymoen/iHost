package no.ntnu.prog2007.ihostapi.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * User model representing a user in the system
 * Stored in both Firebase Auth and Firestore
 *
 * Timestamps are stored as ISO-8601 strings in Firestore to avoid serialization issues
 */
data class User(
    @field:NotBlank(message = "UID is required")
    val uid: String = "",

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String = "",

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 4, max = 12, message = "Username must be between 4 and 12 characters")
    val userName: String = "",

    val phoneNumber: String? = null,

    val photoUrl: String? = null,

    val createdAt: String? = null,

    val updatedAt: String? = null,

    val isEmailVerified: Boolean = false
)
