package no.ntnu.prog2007.ihostapi.model.entity

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * User profile entity stored in Firestore.
 *
 * Represents the application-specific user data that complements Firebase Auth.
 * Firebase Auth handles authentication (email/password, tokens), while this entity
 * stores profile information like username, display name, and profile photo.
 *
 * **Storage**: Firestore `users` collection, where the document ID is the Firebase Auth UID.
 * This links the profile directly to the authenticated user without storing the UID redundantly.
 *
 * **Design decisions**:
 * - Default values enable Firestore deserialization
 * - Username constraints (4-12 chars) balance uniqueness with usability
 *
 * @property email User's email address, must match Firebase Auth email
 * @property username Unique display name for social features (4-12 characters)
 * @property firstName User's first name for personalized greetings
 * @property lastName Optional last name
 * @property photoUrl Cloudinary URL for profile picture, null if not set
 * @property createdAt ISO-8601 timestamp of profile creation
 * @property updatedAt ISO-8601 timestamp of last profile update
 */
data class User(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String = "",

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 4, max = 12, message = "Username must be between 4 and 12 characters")
    val username: String = "",

    @field:NotBlank(message = "First name is required")
    val firstName: String = "",

    val lastName: String? = null,

    val photoUrl: String? = null,

    val createdAt: String? = null,

    val updatedAt: String? = null
)
