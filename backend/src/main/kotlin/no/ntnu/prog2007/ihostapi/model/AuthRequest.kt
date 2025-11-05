package no.ntnu.prog2007.ihostapi.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request for creating user profile after Firebase Auth registration
 * Android registers user in Firebase Auth first, then sends this to create profile
 */
data class CreateUserRequest(
    @field:NotBlank(message = "UID is required")
    val uid: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 4, max = 12, message = "Username must be between 4 and 12 characters")
    val username: String,

    val phoneNumber: String? = null,

    val photoUrl: String? = null
)

/**
 * Response after successful registration
 */
data class AuthResponse(
    val uid: String,
    val email: String,
    val username: String,
    val message: String = "Bruker opprettet"
)