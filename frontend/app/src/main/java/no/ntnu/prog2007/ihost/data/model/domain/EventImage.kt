package no.ntnu.prog2007.ihost.data.model.domain

/**
 * Event image data
 */
data class EventImage(
    val id: String,
    val path: String,
    val eventId: String,
    val createdAt: String
)
