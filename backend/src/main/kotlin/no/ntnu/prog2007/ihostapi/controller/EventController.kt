package no.ntnu.prog2007.ihostapi.controller

import com.google.cloud.firestore.Firestore
import jakarta.validation.Valid
import no.ntnu.prog2007.ihostapi.model.CreateEventRequest
import no.ntnu.prog2007.ihostapi.model.ErrorResponse
import no.ntnu.prog2007.ihostapi.model.Event
import no.ntnu.prog2007.ihostapi.model.UpdateEventRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
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

            val query = firestore.collection(EVENTS_COLLECTION).get().get()
            val events = query.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)
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
                logger.info("Retrieved event: $id for user: $uid")
                ResponseEntity.ok(event)
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
                id = UUID.randomUUID().toString(),
                title = request.title,
                description = request.description,
                eventDate = request.eventDate,
                eventTime = request.eventTime,
                location = request.location,
                creatorUid = uid,
                creatorName = creatorName,
                attendees = listOf(uid), // Creator is automatically an attendee
                free = request.free,
                price = request.price,
                createdAt = timestamp,
                updatedAt = timestamp
            )

            firestore.collection(EVENTS_COLLECTION)
                .document(event.id)
                .set(event)
                .get()

            logger.info("Event created with ID: ${event.id} by user: $uid")
            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(event)
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
            ResponseEntity.ok(updatedEvent)
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

            firestore.collection(EVENTS_COLLECTION)
                .document(id)
                .delete()
                .get()

            logger.info("Event deleted: $id by user: $uid")
            ResponseEntity.ok(mapOf("message" to "Event deleted successfully"))
        } catch (e: Exception) {
            logger.warning("Error deleting event $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not delete event"))
        }
    }

    /**
     * Join an event
     * Add current user to attendees list
     */
    @PostMapping("/{id}/join")
    fun joinEvent(
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

            // Check if already attending
            if (event.attendees.contains(uid)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse("ALREADY_ATTENDING", "Already attending this event"))
            }

            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val timestamp = now.format(formatter)

            val updatedEvent = event.copy(
                attendees = event.attendees + uid,
                updatedAt = timestamp
            )

            firestore.collection(EVENTS_COLLECTION)
                .document(id)
                .set(updatedEvent)
                .get()

            logger.info("User $uid joined event: $id")
            ResponseEntity.ok(updatedEvent)
        } catch (e: Exception) {
            logger.warning("Error joining event $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not join event"))
        }
    }

    /**
     * Leave an event
     * Remove current user from attendees list
     */
    @PostMapping("/{id}/leave")
    fun leaveEvent(
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

            // Check if attending
            if (!event.attendees.contains(uid)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse("NOT_ATTENDING", "Not attending this event"))
            }

            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val timestamp = now.format(formatter)

            val updatedEvent = event.copy(
                attendees = event.attendees.filterNot { it == uid },
                updatedAt = timestamp
            )

            firestore.collection(EVENTS_COLLECTION)
                .document(id)
                .set(updatedEvent)
                .get()

            logger.info("User $uid left event: $id")
            ResponseEntity.ok(updatedEvent)
        } catch (e: Exception) {
            logger.warning("Error leaving event $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("ERROR", "Could not leave event"))
        }
    }
}
