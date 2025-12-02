package no.ntnu.prog2007.ihostapi.model.dto

import jakarta.validation.constraints.NotBlank

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
