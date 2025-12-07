package no.ntnu.prog2007.ihostapi.model.dto

import jakarta.validation.constraints.NotBlank

/**
 * Request payload for creating a new event.
 *
 * The creator UID is not included here as it's extracted from the
 * authenticated user's security context, preventing impersonation.
 *
 * A unique share code (format: IH-XXXXX) is automatically generated
 * server-side to ensure uniqueness and consistent formatting.
 *
 * @property title Event name displayed to users (required)
 * @property description Optional detailed information about the event
 * @property eventDate Date in ISO format YYYY-MM-DD (required)
 * @property eventTime Optional start time in 24-hour format HH:mm
 * @property location Optional venue name or address
 * @property free Whether attendance is free (defaults to true)
 * @property price Cost if not free (ignored when free=true)
 */
data class CreateEventRequest(
    @field:NotBlank(message = "Title is required")
    val title: String,

    val description: String? = null,

    @field:NotBlank(message = "Event date is required")
    val eventDate: String,

    val eventTime: String? = null,

    val location: String? = null,

    val free: Boolean = true,

    val price: Double = 0.0
)

/**
 * Request payload for partial event updates.
 *
 * All fields are optional to support partial updates. Only non-null fields
 * are applied to the existing event. Only the event creator can update events.
 *
 * Note: creatorUid and shareCode cannot be modified after creation
 * to maintain data integrity and share link stability.
 *
 * @property title Updated event name
 * @property description Updated event details
 * @property eventDate Updated date (ISO format YYYY-MM-DD)
 * @property eventTime Updated start time (HH:mm format)
 * @property location Updated venue information
 */
data class UpdateEventRequest(
    val title: String? = null,
    val description: String? = null,
    val eventDate: String? = null,
    val eventTime: String? = null,
    val location: String? = null
)
