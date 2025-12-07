package no.ntnu.prog2007.ihostapi.model.entity

import jakarta.validation.constraints.NotBlank

/**
 * Event entity representing an event in the system.
 *
 * Events are the core feature of iHost, allowing users to create, share, and manage
 * gatherings with friends. The event creator has full control over the event details
 * and can invite other users to attend.
 *
 * **Storage**: Firestore `events` collection with auto-generated document IDs.
 * The document ID serves as the event identifier in API responses.
 *
 * **Attendee management**: User participation is tracked separately in the `event_users`
 * collection via [EventUser] entities. This separation allows efficient querying of
 * events by user and users by event without denormalizing data.
 *
 * **Share code system**: Each event gets a unique code (format: `IH-XXXXX`) that can be
 * shared externally. When a user looks up an event by share code, they're automatically
 * added as a pending attendee, enabling easy event discovery without direct invitations.
 *
 * @property title Display name for the event (required)
 * @property description Optional detailed information about the event
 * @property eventDate Date of the event in ISO format (YYYY-MM-DD)
 * @property eventTime Optional start time in 24-hour format (HH:mm)
 * @property location Optional venue or address
 * @property creatorUid Firebase UID of the user who created the event
 * @property free Whether the event is free to attend (affects price display)
 * @property price Ticket price if not free, defaults to 0.0
 * @property createdAt ISO-8601 timestamp of event creation
 * @property updatedAt ISO-8601 timestamp of last modification
 * @property shareCode Unique shareable code for event discovery (format: IH-XXXXX)
 *
 * @see EventUser for attendee tracking
 * @see no.ntnu.prog2007.ihostapi.service.impl.EventServiceImpl for share code generation
 */
data class Event(
    @field:NotBlank(message = "Title is required")
    val title: String = "",

    val description: String? = null,

    @field:NotBlank(message = "Event date is required")
    val eventDate: String = "",

    val eventTime: String? = null,

    val location: String? = null,

    @field:NotBlank(message = "Creator UID is required")
    val creatorUid: String = "",

    val free: Boolean = true,

    val price: Double = 0.0,

    val createdAt: String? = null,

    val updatedAt: String? = null,

    val shareCode: String = ""
)
