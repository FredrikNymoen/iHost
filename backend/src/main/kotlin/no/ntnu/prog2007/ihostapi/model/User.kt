package no.ntnu.prog2007.ihostapi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

/**
 * User model representing a user in the system
 * Stored in both Firebase Auth and Firestore
 */
data class User(
    @field:NotBlank(message = "UID is required")
    val uid: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Display name is required")
    val displayName: String,

    val phoneNumber: String? = null,

    val photoUrl: String? = null,

    @JsonIgnore
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @JsonIgnore
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    val isEmailVerified: Boolean = false
)
