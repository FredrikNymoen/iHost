package no.ntnu.prog2007.ihostapi.model

import jakarta.validation.constraints.NotBlank
import java.util.*

/**
 * Event model representing an event in the system
 * Stored in Firestore
 * Note: Document ID is used as the event identifier, not stored in the model
 * Note: Attendees are managed through the event_users collection
 */
data class Event(
    @field:NotBlank(message = "Title is required")
    val title: String = "",

    val description: String? = null,

    @field:NotBlank(message = "Event date is required")
    val eventDate: String = "", // ISO format YYYY-MM-DD

    val eventTime: String? = null, // HH:mm format

    val location: String? = null,

    @field:NotBlank(message = "Creator UID is required")
    val creatorUid: String = "",

    val creatorName: String? = null,

    val free: Boolean = true,

    val price: Double = 0.0,

    val createdAt: String? = null,

    val updatedAt: String? = null,

    val shareCode: String = "" // Unique code for sharing event with other people
)

/**
 * Request for creating an event
 */
data class CreateEventRequest(
    @field:NotBlank(message = "Title is required")
    val title: String,

    val description: String? = null,

    @field:NotBlank(message = "Event date is required")
    val eventDate: String, // ISO format YYYY-MM-DD

    val eventTime: String? = null, // HH:mm format

    val location: String? = null,

    val free: Boolean = true,

    val price: Double = 0.0
)

/**
 * Request for updating an event
 */
data class UpdateEventRequest(
    val title: String? = null,
    val description: String? = null,
    val eventDate: String? = null,
    val eventTime: String? = null,
    val location: String? = null
)
