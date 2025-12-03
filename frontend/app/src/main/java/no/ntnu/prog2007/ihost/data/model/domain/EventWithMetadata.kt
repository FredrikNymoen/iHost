package no.ntnu.prog2007.ihost.data.model.domain

/**
 * Event with metadata about user's relationship to the event
 */
data class EventWithMetadata(
    val id: String,
    val event: Event,
    val userStatus: String? = null,
    val userRole: String? = null
)
