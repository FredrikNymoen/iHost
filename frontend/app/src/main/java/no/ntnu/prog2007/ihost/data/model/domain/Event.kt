package no.ntnu.prog2007.ihost.data.model.domain

/**
 * Event domain model
 * Note: ID is stored separately in EventWithMetadata wrapper
 * Note: Attendees are managed through event_users collection
 */
data class Event(
    val title: String,
    val description: String? = null,
    val eventDate: String,
    val eventTime: String? = null,
    val location: String? = null,
    val creatorUid: String,
    val free: Boolean = true,
    val price: Double = 0.0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val shareCode: String = ""
)
