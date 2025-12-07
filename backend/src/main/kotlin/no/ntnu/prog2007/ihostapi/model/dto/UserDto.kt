package no.ntnu.prog2007.ihostapi.model.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request payload for creating a user profile after Firebase Auth registration.
 *
 * The registration flow is split between client and server:
 * 1. Android app registers user with Firebase Auth (handles password, email verification)
 * 2. Client sends this request to create the application profile
 *
 * This separation ensures Firebase Auth remains the source of truth for authentication
 * while the backend manages application-specific profile data.
 *
 * @property uid Firebase Auth UID, used as the Firestore document ID
 * @property email Must match the Firebase Auth email for consistency
 * @property username Unique identifier for social features (4-12 chars)
 * @property firstName Display name for personalization
 * @property lastName Optional family name
 * @property photoUrl Optional profile picture URL (typically from Cloudinary)
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

    @field:NotBlank(message = "First name is required")
    val firstName: String,
    val lastName: String? = null,

    val photoUrl: String? = null
)

/**
 * Response returned after successful user registration.
 *
 * Confirms the profile was created and provides key identifiers
 * for the client to store and use in subsequent requests.
 *
 * @property uid Firebase Auth UID for authenticated requests
 * @property email Confirmed email address
 * @property username Confirmed unique username
 * @property message Human-readable success message (Norwegian: "Bruker opprettet")
 */
data class AuthResponse(
    val uid: String,
    val email: String,
    val username: String,
    val message: String = "Bruker opprettet"
)

/**
 * Request payload for partial user profile updates.
 *
 * All fields are optional to support partial updates. Only provided fields
 * will be updated; null fields are ignored (not set to null in the database).
 *
 * Note: Email and username cannot be changed after registration to maintain
 * consistency with Firebase Auth and prevent identity confusion.
 *
 * @property firstName Updated first name, or null to keep current
 * @property lastName Updated last name, or null to keep current
 * @property photoUrl Updated profile picture URL, or null to keep current
 */
data class UpdateUserRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val photoUrl: String? = null
)

/**
 * User data response for API endpoints.
 *
 * Combines the Firebase UID (from document ID) with profile fields,
 * providing complete user information for display in the client app.
 *
 * @property uid Firebase Auth UID (from Firestore document ID)
 * @property email User's email address
 * @property username Unique display name
 * @property firstName User's first name
 * @property lastName Optional last name
 * @property photoUrl Cloudinary URL for profile picture
 * @property createdAt ISO-8601 timestamp of account creation
 * @property updatedAt ISO-8601 timestamp of last profile update
 */
data class UserResponse(
    val uid: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String? = null,
    val photoUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
