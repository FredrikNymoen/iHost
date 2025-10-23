package no.ntnu.prog2007.ihostapi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime
import java.util.*

/**
 * Event model representing an event in the system
 * Stored in Firestore
 */
data class Event(
    @field:NotBlank(message = "ID is required")
    val id: String = UUID.randomUUID().toString(),

    @field:NotBlank(message = "Title is required")
    val title: String,

    val description: String? = null,

    @field:NotBlank(message = "Event date is required")
    val eventDate: String, // ISO format YYYY-MM-DD

    val eventTime: String? = null, // HH:mm format

    val location: String? = null,

    @field:NotBlank(message = "Creator UID is required")
    val creatorUid: String,

    val creatorName: String? = null,

    val attendees: List<String> = emptyList(),

    @JsonIgnore
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @JsonIgnore
    val updatedAt: LocalDateTime = LocalDateTime.now()
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

    val location: String? = null
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
