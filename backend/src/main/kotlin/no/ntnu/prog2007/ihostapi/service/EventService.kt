package no.ntnu.prog2007.ihostapi.service

import no.ntnu.prog2007.ihostapi.model.dto.CreateEventRequest
import no.ntnu.prog2007.ihostapi.model.dto.UpdateEventRequest
import no.ntnu.prog2007.ihostapi.model.entity.Event

/**
 * Service interface for Event operations
 */
interface EventService {
    fun getAllEventsForUser(userId: String): List<Map<String, Any?>>
    fun getEventById(eventId: String, userId: String): Map<String, Any?>?
    fun createEvent(request: CreateEventRequest, creatorUid: String): Pair<String, Event>
    fun updateEvent(eventId: String, request: UpdateEventRequest, userId: String): Event
    fun deleteEvent(eventId: String, userId: String): Int
    fun findEventByShareCode(shareCode: String, userId: String): Map<String, Any?>?
}
