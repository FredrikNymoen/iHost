package no.ntnu.prog2007.ihostapi.model.entity

/**
 * Junction entity linking users to events they're associated with.
 *
 * Acts as a many-to-many relationship table between users and events, storing
 * both the relationship metadata (when invited, response status) and the user's
 * role in the event. This design enables:
 * - Querying all events for a user (with their status in each)
 * - Querying all attendees for an event (with their RSVP status)
 * - Tracking invitation/response timestamps for activity feeds
 *
 * **Storage**: Firestore `event_users` collection with auto-generated document IDs.
 * The document ID is returned in API responses via [EventUserResponse].
 *
 * **Creation scenarios**:
 * 1. Event creation → CREATOR status, CREATOR role (automatic)
 * 2. Direct invitation → PENDING status, ATTENDEE role
 * 3. Share code lookup → PENDING status, ATTENDEE role (auto-invite)
 *
 * @property eventId Reference to the event document ID
 * @property userId Firebase UID of the associated user
 * @property status Current RSVP state (see [EventUserStatus])
 * @property role User's permission level in the event (see [EventUserRole])
 * @property invitedAt ISO-8601 timestamp when the association was created
 * @property respondedAt ISO-8601 timestamp when user accepted/declined (null if pending)
 *
 * @see EventUserStatus for possible status values
 * @see EventUserRole for permission levels
 */
data class EventUser(
    val eventId: String = "",
    val userId: String = "",
    val status: EventUserStatus = EventUserStatus.PENDING,
    val role: EventUserRole = EventUserRole.ATTENDEE,
    val invitedAt: String = "",
    val respondedAt: String? = null
)

/**
 * RSVP status for a user's event invitation.
 *
 * Tracks the user's response to an event invitation through a simple state machine:
 * - PENDING → ACCEPTED (user accepts)
 * - PENDING → DECLINED (user declines)
 * - CREATOR (immutable, set at event creation)
 */
enum class EventUserStatus {
    /** User has been invited but hasn't responded yet */
    PENDING,
    /** User has accepted the invitation and will attend */
    ACCEPTED,
    /** User has declined the invitation */
    DECLINED,
    /** User created this event (cannot change status) */
    CREATOR
}

/**
 * Permission role for a user within an event.
 *
 * Determines what actions a user can perform on the event:
 * - CREATOR: Full control (edit, delete, invite others)
 * - ATTENDEE: View-only access, can accept/decline their invitation
 */
enum class EventUserRole {
    /** Event host with full management permissions */
    CREATOR,
    /** Regular participant with view-only access */
    ATTENDEE
}
