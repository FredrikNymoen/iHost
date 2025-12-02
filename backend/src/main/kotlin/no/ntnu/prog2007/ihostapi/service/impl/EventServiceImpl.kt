package no.ntnu.prog2007.ihostapi.service.impl

import no.ntnu.prog2007.ihostapi.exception.ResourceNotFoundException
import no.ntnu.prog2007.ihostapi.model.dto.CreateEventRequest
import no.ntnu.prog2007.ihostapi.model.dto.UpdateEventRequest
import no.ntnu.prog2007.ihostapi.model.entity.Event
import no.ntnu.prog2007.ihostapi.model.entity.EventUser
import no.ntnu.prog2007.ihostapi.model.entity.EventUserRole
import no.ntnu.prog2007.ihostapi.model.entity.EventUserStatus
import no.ntnu.prog2007.ihostapi.repository.EventRepository
import no.ntnu.prog2007.ihostapi.repository.EventUserRepository
import no.ntnu.prog2007.ihostapi.service.EventService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

/**
 * Service implementation for Event operations
 */
@Service
class EventServiceImpl(
    private val eventRepository: EventRepository,
    private val eventUserRepository: EventUserRepository
) : EventService {
    private val logger = Logger.getLogger(EventServiceImpl::class.java.name)

    override fun getAllEventsForUser(userId: String): List<Map<String, Any?>> {
        val eventUsers = eventUserRepository.findByUserId(userId)

        return eventUsers.mapNotNull { (_, eventUser) ->
            try {
                val event = eventRepository.findById(eventUser.eventId)
                event?.let {
                    val eventId = eventUser.eventId
                    mapOf(
                        "id" to eventId,
                        "event" to event,
                        "userStatus" to eventUser.status,
                        "userRole" to eventUser.role
                    )
                }
            } catch (e: Exception) {
                logger.warning("Failed to fetch event ${eventUser.eventId}: ${e.message}")
                null
            }
        }
    }

    override fun getEventById(eventId: String, userId: String): Map<String, Any?>? {
        val event = eventRepository.findById(eventId)
            ?: throw ResourceNotFoundException("Event not found")

        // Check if user has an event_user entry for this event
        val (userStatus, userRole) = eventUserRepository.findByEventIdAndUserId(eventId, userId)?.let { (_, eventUser) ->
            Pair(eventUser.status, eventUser.role)
        } ?: Pair(null, null)

        return mapOf(
            "id" to eventId,
            "event" to event,
            "userStatus" to userStatus,
            "userRole" to userRole
        )
    }

    override fun createEvent(request: CreateEventRequest, creatorUid: String): Pair<String, Event> {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val timestamp = now.format(formatter)

        val event = Event(
            title = request.title,
            description = request.description,
            eventDate = request.eventDate,
            eventTime = request.eventTime,
            location = request.location,
            creatorUid = creatorUid,
            free = request.free,
            price = request.price,
            createdAt = timestamp,
            updatedAt = timestamp,
            shareCode = generateShareCode()
        )

        // Save event
        val (eventId, savedEvent) = eventRepository.save(event)

        // Create event_user document for creator
        val eventUser = EventUser(
            eventId = eventId,
            userId = creatorUid,
            status = EventUserStatus.CREATOR,
            role = EventUserRole.CREATOR,
            invitedAt = timestamp,
            respondedAt = timestamp
        )

        eventUserRepository.save(eventUser)

        logger.info("Event created with ID: $eventId by user: $creatorUid")
        return Pair(eventId, savedEvent)
    }

    override fun updateEvent(eventId: String, request: UpdateEventRequest, userId: String): Event {
        val event = eventRepository.findById(eventId)
            ?: throw ResourceNotFoundException("Event not found")

        // Check if user is the creator
        if (event.creatorUid != userId) {
            throw IllegalArgumentException("Only the creator can update this event")
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

        eventRepository.update(eventId, updatedEvent)

        logger.info("Event updated: $eventId by user: $userId")
        return updatedEvent
    }

    override fun deleteEvent(eventId: String, userId: String): Int {
        val event = eventRepository.findById(eventId)
            ?: throw ResourceNotFoundException("Event not found")

        // Check if user is the creator
        if (event.creatorUid != userId) {
            throw IllegalArgumentException("Only the creator can delete this event")
        }

        // Delete event_users and event
        val deletedCount = eventUserRepository.deleteByEventId(eventId)
        eventRepository.delete(eventId)

        logger.info("Event deleted: $eventId by user: $userId with $deletedCount related event_users")
        return deletedCount
    }

    override fun findEventByShareCode(shareCode: String, userId: String): Map<String, Any?>? {
        val (eventId, event) = eventRepository.findByShareCode(shareCode)
            ?: throw ResourceNotFoundException("No event found with code: $shareCode")

        // Check if user has an event_user entry for this event
        val existingEventUser = eventUserRepository.findByEventIdAndUserId(eventId, userId)

        val (userStatus, userRole) = if (existingEventUser != null) {
            val (_, eventUser) = existingEventUser
            Pair(eventUser.status, eventUser.role)
        } else {
            // User doesn't have an event_user entry
            if (event.creatorUid == userId) {
                // Creator fetching their own event
                logger.warning("Creator $userId fetching their own event $eventId via share code without event_user entry")
                Pair(null, null)
            } else {
                // Regular user finding event via share code - create PENDING event_user
                val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val newEventUser = EventUser(
                    eventId = eventId,
                    userId = userId,
                    status = EventUserStatus.PENDING,
                    role = EventUserRole.ATTENDEE,
                    invitedAt = now,
                    respondedAt = null
                )

                eventUserRepository.save(newEventUser)

                logger.info("Created PENDING event_user for user $userId on event $eventId")
                Pair(EventUserStatus.PENDING, EventUserRole.ATTENDEE)
            }
        }

        logger.info("Event found by code $shareCode; $eventId for user: $userId")
        return mapOf(
            "id" to eventId,
            "event" to event,
            "userStatus" to userStatus,
            "userRole" to userRole
        )
    }

    /**
     * Function to generate a unique share code for a specific event
     * Format is PREFIX-SUFFIX where Prefix is "IH" for IHost
     * and suffix is a 5 character long random combination of uppercase letters and digits
     */
    private fun generateShareCode(): String {
        val charPool = ('A'..'Z') + ('0'..'9')
        val suffix = (1..5)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")

        return "IH-$suffix"
    }
}