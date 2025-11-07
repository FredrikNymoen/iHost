package no.ntnu.prog2007.ihostapi.controller

import com.google.cloud.firestore.Firestore
import jakarta.validation.Valid
import no.ntnu.prog2007.ihostapi.model.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger

@RestController
@RequestMapping("/api/events")
class EventController(
    private val firestore: Firestore
) {
    private val logger = Logger.getLogger(EventController::class.java.name)

    companion object {
        const val EVENTS_COLLECTION = "events"
        const val EVENT_USERS_COLLECTION = "event_users"
    }

    /**
     * Get all events
     * Requires valid Firebase JWT token in Authorization header
     */
    @GetMapping
    fun getAllEvents(): ResponseEntity<Any> {
        return try {
            val uid = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            // Get events where user is invited (from event_users collection)
            val eventUsersQuery = firestore.collection(EVENT_USERS_COLLECTION)
                .whereEqualTo("userId", uid)
                .get()
                .get()

            val eventUsers = eventUsersQuery.documents.mapNotNull { doc ->
                doc.toObject(EventUser::class.java)
            }

            // Fetch the actual event details for each
            val events = eventUsers.mapNotNull { eventUser ->
                try {
                    val eventDoc = firestore.collection(EVENTS_COLLECTION)
                        .document(eventUser.eventId)
                        .get()
                        .get()

                    if (eventDoc.exists()) {
                        val event = eventDoc.toObject(Event::class.java)
                        mapOf(
                            "id" to eventDoc.id,
                            "event" to event,
                            "userStatus" to eventUser.status,
                            "userRole" to eventUser.role
                        )
                    } else null
                } catch (e: Exception) {
                    logger.warning("Failed to fetch event ${eventUser.eventId}: ${e.message}")
                    null
                }
            }

            logger.info("Retrieved ${events.size} events for user: $uid")
            ResponseEntity.ok(events)
        } catch (e: Exception) {
            logger.warning("Error getting events: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not retrieve events"))
        }
    }

    /**
     * Get event by ID
     */
    @GetMapping("/{id}")
    fun getEventById(
        @PathVariable id: String
    ): ResponseEntity<Any> {
        return try {
            val uid = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            val eventDoc = firestore.collection(EVENTS_COLLECTION)
                .document(id)
                .get()
                .get()

            if (!eventDoc.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "Event not found"))
            }

            val event = eventDoc.toObject(Event::class.java)
            if (event != null) {
                // Check if user has an event_user entry for this event
                val eventUserQuery = firestore.collection(EVENT_USERS_COLLECTION)
                    .whereEqualTo("eventId", id)
                    .whereEqualTo("userId", uid)
                    .limit(1)
                    .get()
                    .get()

                val (userStatus, userRole) = if (!eventUserQuery.documents.isEmpty()) {
                    val eventUser = eventUserQuery.documents[0].toObject(EventUser::class.java)
                    Pair(eventUser?.status, eventUser?.role)
                } else {
                    Pair(null, null)
                }

                logger.info("Retrieved event: $id for user: $uid")
                ResponseEntity.ok(mapOf(
                    "id" to id,
                    "event" to event,
                    "userStatus" to userStatus,
                    "userRole" to userRole
                ))
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse("ERROR", "Could not parse event data"))
            }
        } catch (e: Exception) {
            logger.warning("Error getting event $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not retrieve event"))
        }
    }

    /**
     * Create a new event
     * Requires valid Firebase JWT token in Authorization header
     */
    @PostMapping
    fun createEvent(
        @Valid @RequestBody request: CreateEventRequest
    ): ResponseEntity<Any> {
        return try {
            val uid = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            // Get creator name from Firestore
            val userDoc = firestore.collection("users")
                .document(uid)
                .get()
                .get()
            val creatorName = if (userDoc.exists()) {
                userDoc.getString("displayName") ?: "Anonymous"
            } else {
                "Anonymous"
            }

            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val timestamp = now.format(formatter)

            val event = Event(
                title = request.title,
                description = request.description,
                eventDate = request.eventDate,
                eventTime = request.eventTime,
                location = request.location,
                creatorUid = uid,
                creatorName = creatorName,
                free = request.free,
                price = request.price,
                createdAt = timestamp,
                updatedAt = timestamp,
                shareCode = generateShareCode() // Generate unique share code, see function below
            )

            // Create event document
            val eventRef = firestore.collection(EVENTS_COLLECTION).document()
            eventRef.set(event).get()

            // Create event_user document for creator
            val eventUser = EventUser(
                eventId = eventRef.id,
                userId = uid,
                status = EventUserStatus.CREATOR,
                role = EventUserRole.CREATOR,
                invitedAt = timestamp,
                respondedAt = timestamp
            )

            firestore.collection(EVENT_USERS_COLLECTION)
                .document()
                .set(eventUser)
                .get()

            logger.info("Event created with ID: ${eventRef.id} by user: $uid")
            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapOf(
                    "id" to eventRef.id,
                    "event" to event,
                    "userStatus" to EventUserStatus.CREATOR,
                    "userRole" to EventUserRole.CREATOR
                ))
        } catch (e: Exception) {
            logger.warning("Error creating event: ${e.message}")
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                    ErrorResponse(
                        error = "CREATION_FAILED",
                        message = e.message ?: "Failed to create event"
                    )
                )
        }
    }

    /**
     * Update an event
     * Only the creator can update the event
     */
    @PutMapping("/{id}")
    fun updateEvent(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateEventRequest
    ): ResponseEntity<Any> {
        return try {
            val uid = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            val eventDoc = firestore.collection(EVENTS_COLLECTION)
                .document(id)
                .get()
                .get()

            if (!eventDoc.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "Event not found"))
            }

            val event = eventDoc.toObject(Event::class.java)
                ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse("ERROR", "Could not parse event data"))

            // Check if user is the creator
            if (event.creatorUid != uid) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse("FORBIDDEN", "Only the creator can update this event"))
            }

            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val timestamp = now.format(formatter)

            // Update only non-null fields
            val updatedEvent = event.copy(
                title = request.title ?: event.title,
                description = request.description ?: event.description,
                eventDate = request.eventDate ?: event.eventDate,
                eventTime = request.eventTime ?: event.eventTime,
                location = request.location ?: event.location,
                updatedAt = timestamp
            )

            firestore.collection(EVENTS_COLLECTION)
                .document(id)
                .set(updatedEvent)
                .get()

            logger.info("Event updated: $id by user: $uid")
            ResponseEntity.ok(mapOf(
                "id" to id,
                "event" to updatedEvent,
                "userStatus" to EventUserStatus.CREATOR,
                "userRole" to EventUserRole.CREATOR
            ))
        } catch (e: Exception) {
            logger.warning("Error updating event $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not update event"))
        }
    }

    /**
     * Delete an event
     * Only the creator can delete the event
     */
    @DeleteMapping("/{id}")
    fun deleteEvent(
        @PathVariable id: String
    ): ResponseEntity<Any> {
        return try {
            val uid = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            val eventDoc = firestore.collection(EVENTS_COLLECTION)
                .document(id)
                .get()
                .get()

            if (!eventDoc.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "Event not found"))
            }

            val event = eventDoc.toObject(Event::class.java)
                ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse("ERROR", "Could not parse event data"))

            // Check if user is the creator
            if (event.creatorUid != uid) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse("FORBIDDEN", "Only the creator can delete this event"))
            }

            // Create batch operation for atomic deletion and cascading deletion of event_users to fix orphaned entries issue
            val batch = firestore.batch()

            // Find all event_users corresponding to this event
            val eventUsersQuery = firestore.collection(EVENT_USERS_COLLECTION)
                .whereEqualTo("eventId", id)
                .get()
                .get()

            // Count how many records we find for more detailed logging to help identify issues
            val eventUsersCount = eventUsersQuery.size()

            //Add all event_users to the batch to prepare for deletion
            for (doc in eventUsersQuery.documents) {
                batch.delete(doc.reference)
            }

            // Add the event itself
            batch.delete(firestore.collection(EVENTS_COLLECTION).document(id))

            // Finally execute the batch and log it
            batch.commit().get()

            logger.info("Event deleted: $id by user: $uid with $eventUsersCount related event_users")

            ResponseEntity.ok(mapOf("message" to "Event deleted successfully", "deletedEventUsers" to eventUsersCount))
        } catch (e: Exception) {
            logger.warning("Error deleting event $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not delete event"))
        }
    }

    // Join/leave functionality moved to EventUserController
    // Use /api/event-users/{eventUserId}/accept or /decline instead

    /**
     * Find event by share code
     * Returns event matching the share code, or 404 if event is not found
     */
    @GetMapping("/by-code/{shareCode}")
    fun findEventByCode(
        @PathVariable shareCode: String
    ): ResponseEntity<Any> {
        return try {
            // Find uid from token in security context
            val uid = SecurityContextHolder.getContext().authentication.principal as? String
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("UNAUTHORIZED", "Token is invalid or missing"))

            // Query Firestore for event with matching share code
            val query = firestore.collection(EVENTS_COLLECTION)
                .whereEqualTo("shareCode", shareCode)
                .limit(1)
                .get()
                .get()

            // If no documents found, return 404
            if (query.documents.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse("NOT_FOUND", "No event found with code: $shareCode"))
            }

            // Parse event from document
            val eventDoc = query.documents[0]
            val event = eventDoc.toObject(Event::class.java)

            // Check if user has an event_user entry for this event
            val eventUserQuery = firestore.collection(EVENT_USERS_COLLECTION)
                .whereEqualTo("eventId", eventDoc.id)
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .get()

            val (userStatus, userRole) = if (!eventUserQuery.documents.isEmpty()) {
                // User already has an event_user entry
                val eventUser = eventUserQuery.documents[0].toObject(EventUser::class.java)
                Pair(eventUser.status, eventUser?.role)
            } else {
                // User doesn't have an event_user entry
                // Check if user is the creator - don't create event_user for creator fetching their own event
                if (event.creatorUid == uid) {
                    // Creator is fetching their own event via share code - should already have CREATOR event_user
                    // This shouldn't happen normally, but handle gracefully
                    logger.warning("Creator $uid fetching their own event ${eventDoc.id} via share code without event_user entry")
                    Pair(null, null)
                } else {
                    // Regular user finding event via share code - create PENDING event_user
                    val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    val newEventUser = EventUser(
                        eventId = eventDoc.id,
                        userId = uid,
                        status = EventUserStatus.PENDING,
                        role = EventUserRole.ATTENDEE,
                        invitedAt = now,
                        respondedAt = null
                    )

                    // Save to Firestore
                    firestore.collection(EVENT_USERS_COLLECTION)
                        .document()
                        .set(newEventUser)
                        .get()

                    logger.info("Created PENDING event_user for user $uid on event ${eventDoc.id}")
                    Pair(EventUserStatus.PENDING, EventUserRole.ATTENDEE)
                }
            }

            // log and return event
            logger.info("Event found by code $shareCode; ${eventDoc.id} for user: $uid")
            ResponseEntity.ok(mapOf(
                "id" to eventDoc.id,
                "event" to event,
                "userStatus" to userStatus,
                "userRole" to userRole
            ))
        } catch (e: Exception) { // Catch any errors
            logger.warning("Error finding event by code $shareCode: ${e.message}") // Log warning
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // Return 500
                .body(ErrorResponse("ERROR", "Could not retrieve event by code"))
        }
    }
}

/**
 * Function to generate a unique share code for a specific event
 * Format is PREFIX-SUFFIX where Prefix is "IH" for IHost
 * and suffix is a 5 character long random combination of uppercase letters and digits
 * i.e. IH-A1B2C
 * @return generated share code
 */
private fun generateShareCode(): String {
    val charPool = ('A'..'Z') + ('0'..'9') // Uppercase letters and digits
    val suffix = (1..5) // Generate 5 characters
        .map { kotlin.random.Random.nextInt(0, charPool.size) } // Random indices
        .map(charPool::get) // Map each index to random character from pool
        .joinToString("") // Join characters to form suffix

    return "IH-$suffix" // Return prefix-suffix for the full code
}
