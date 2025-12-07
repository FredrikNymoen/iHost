package no.ntnu.prog2007.ihostapi.service.impl

import io.mockk.*
import no.ntnu.prog2007.ihostapi.exception.ResourceNotFoundException
import no.ntnu.prog2007.ihostapi.model.dto.CreateEventRequest
import no.ntnu.prog2007.ihostapi.model.dto.UpdateEventRequest
import no.ntnu.prog2007.ihostapi.model.entity.Event
import no.ntnu.prog2007.ihostapi.model.entity.EventUser
import no.ntnu.prog2007.ihostapi.model.entity.EventUserRole
import no.ntnu.prog2007.ihostapi.model.entity.EventUserStatus
import no.ntnu.prog2007.ihostapi.repository.EventRepository
import no.ntnu.prog2007.ihostapi.repository.EventUserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for EventServiceImpl
 *
 * AAA pattern: Arrange, Act, Assert
 *
 * Mockk is used for mocking dependencies
 * 
 * Testing following cases:
 * - Test share code generation format (IH-XXXXX)
 * - Test creator authorization for updates/deletes
 * - Test event creation workflow
 * - Test partial updates (only non-null fields updated)
 */
@DisplayName("EventServiceImpl Tests")
class EventServiceImplTest {
    // Mocks for dependencies

    private lateinit var eventRepository: EventRepository
    private lateinit var eventUserRepository: EventUserRepository
    // Service under test
    private lateinit var eventService: EventServiceImpl

    @BeforeEach
    fun setup() {
        // Initialize mocks
        eventRepository = mockk()
        eventUserRepository = mockk()
        // Initialize service with mocked dependencies
        eventService = EventServiceImpl(eventRepository, eventUserRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks() // Clear mocks after each test
    }

    /**
     * Tests for createEvent()
     */
    @Nested
    @DisplayName("Create Event Tests")
    inner class CreateEventTests {

        @Test
        @DisplayName("Should create event with valid share code format (IH-XXXXX)")
        fun testCreateEvent_GeneratesValidShareCode() {
            // Arrange
            val creatorId = "user123"
            val request = CreateEventRequest(
                title = "Summer Party",
                description = "Beach party!",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Santa Monica Beach",
                free = true,
                price = 0.0
            )

            val generatedEventId = "event123"
            
            // Mock repository save to capture the event being saved
            val savedEventSlot = slot<Event>()
            every { eventRepository.save(capture(savedEventSlot)) } answers {
                Pair(generatedEventId, savedEventSlot.captured)
            }
            
            every { eventUserRepository.save(any()) } returns Pair("eventUser123", mockk())

            // Act
            val (eventId, event) = eventService.createEvent(request, creatorId)

            // Assert
            assertNotNull(eventId)
            assertEquals(generatedEventId, eventId)
            
            // Verify share code format: IH-XXXXX (2 letters + dash + 5 alphanumeric chars)
            val shareCode = event.shareCode
            assertNotNull(shareCode, "Share code should not be null")
            assertTrue(shareCode.matches(Regex("^IH-[A-Z0-9]{5}$")), 
                "Share code should match format IH-XXXXX, but was: $shareCode")
            
            // Verify event properties
            assertEquals(request.title, event.title)
            assertEquals(request.description, event.description)
            assertEquals(creatorId, event.creatorUid)
            assertEquals(request.free, event.free)
            assertNotNull(event.createdAt)
            assertNotNull(event.updatedAt)

            // Verify repository interactions
            verify(exactly = 1) { eventRepository.save(any()) }
            verify(exactly = 1) { eventUserRepository.save(any()) }
        }

        @Test
        @DisplayName("Should create event_user with CREATOR status for event creator")
        fun testCreateEvent_CreatesEventUserForCreator() {
            // Arrange
            val creatorId = "user123"
            val request = CreateEventRequest(
                title = "Test Event",
                description = "Test",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Test Location",
                free = true,
                price = 0.0
            )

            every { eventRepository.save(any()) } returns Pair("event123", mockk(relaxed = true))
            
            // Capture the EventUser being saved
            val eventUserSlot = slot<EventUser>()
            every { eventUserRepository.save(capture(eventUserSlot)) } returns Pair("eventUser123", mockk(relaxed = true))

            // Act
            eventService.createEvent(request, creatorId)

            // Assert - verify the EventUser has correct properties
            val capturedEventUser = eventUserSlot.captured
            assertEquals(creatorId, capturedEventUser.userId)
            assertEquals(EventUserStatus.CREATOR, capturedEventUser.status)
            assertEquals(EventUserRole.CREATOR, capturedEventUser.role)
            assertNotNull(capturedEventUser.invitedAt)
            assertNotNull(capturedEventUser.respondedAt, "Creator should have respondedAt set immediately")
        }
    }

    /**
     * Tests for updateEvent()
     */
    @Nested
    @DisplayName("Update Event Tests")
    inner class UpdateEventTests {

        @Test
        @DisplayName("Should throw exception when event doesn't exist")
        fun testUpdateEvent_NotFound_ThrowsException() {
            // Arrange
            val eventId = "nonexistent123"
            val userId = "user123"
            val request = UpdateEventRequest(title = "New Title")

            every { eventRepository.findById(eventId) } returns null

            // Act & Assert
            assertThrows<ResourceNotFoundException> {
                eventService.updateEvent(eventId, request, userId)
            }

            verify { eventRepository.findById(eventId) }
        }

        @Test
        @DisplayName("Should throw exception when non-creator tries to update")
        fun testUpdateEvent_NotCreator_ThrowsException() {
            // Arrange
            val eventId = "event123"
            val creatorId = "user123"
            val otherUserId = "user456"
            val event = Event(
                title = "Original Title",
                description = "Original description",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Original location",
                creatorUid = creatorId,
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = "IH-ABC12"
            )
            val request = UpdateEventRequest(title = "New Title")

            every { eventRepository.findById(eventId) } returns event

            // Act & Assert
            val exception = assertThrows<IllegalArgumentException> {
                eventService.updateEvent(eventId, request, otherUserId)
            }

            assertEquals("Only the creator can update this event", exception.message)
            
            // Verify update was never called
            verify(exactly = 0) { eventRepository.update(any(), any()) }
        }

        @Test
        @DisplayName("Should successfully update event when user is creator")
        fun testUpdateEvent_AsCreator_UpdatesSuccessfully() {
            // Arrange
            val eventId = "event123"
            val creatorId = "user123"
            val originalEvent = Event(
                title = "Original Title",
                description = "Original description",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Original location",
                creatorUid = creatorId,
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = "IH-ABC12"
            )
            val request = UpdateEventRequest(
                title = "Updated Title",
                description = "Updated description",
                eventDate = null,
                eventTime = null,
                location = null
            )

            every { eventRepository.findById(eventId) } returns originalEvent
            every { eventRepository.update(eventId, any()) } answers { secondArg() } // Return updated event

            // Act
            val updatedEvent = eventService.updateEvent(eventId, request, creatorId)

            // Assert
            assertEquals("Updated Title", updatedEvent.title)
            assertEquals("Updated description", updatedEvent.description)
            // Non-updated fields should remain the same
            assertEquals(originalEvent.eventDate, updatedEvent.eventDate)
            assertEquals(originalEvent.eventTime, updatedEvent.eventTime)
            assertEquals(originalEvent.location, updatedEvent.location)
            assertEquals(originalEvent.shareCode, updatedEvent.shareCode)
            assertEquals(originalEvent.creatorUid, updatedEvent.creatorUid)
            
            // updatedAt should be changed
            assertNotEquals(originalEvent.updatedAt, updatedEvent.updatedAt)

            verify(exactly = 1) { eventRepository.update(eventId, any()) }
        }

        @Test
        @DisplayName("Should only update non-null fields (partial update)")
        fun testUpdateEvent_PartialUpdate_OnlyUpdatesProvidedFields() {
            // Arrange
            val eventId = "event123"
            val creatorId = "user123"
            val originalEvent = Event(
                title = "Original Title",
                description = "Original description",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Original location",
                creatorUid = creatorId,
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = "IH-ABC12"
            )
            
            // Only update the title, leave everything else null
            val request = UpdateEventRequest(
                title = "New Title Only",
                description = null,
                eventDate = null,
                eventTime = null,
                location = null
            )

            every { eventRepository.findById(eventId) } returns originalEvent
            every { eventRepository.update(eventId, any()) } answers { secondArg() } // Return updated event

            // Act
            val updatedEvent = eventService.updateEvent(eventId, request, creatorId)

            // Assert - only title changed, everything else preserved
            assertEquals("New Title Only", updatedEvent.title)
            assertEquals(originalEvent.description, updatedEvent.description)
            assertEquals(originalEvent.eventDate, updatedEvent.eventDate)
            assertEquals(originalEvent.eventTime, updatedEvent.eventTime)
            assertEquals(originalEvent.location, updatedEvent.location)
        }
    }

    /**
     * Tests for deleteEvent()
     */
    @Nested
    @DisplayName("Delete Event Tests")
    inner class DeleteEventTests {

        @Test
        @DisplayName("Should throw exception when event doesn't exist")
        fun testDeleteEvent_NotFound_ThrowsException() {
            // Arrange
            val eventId = "nonexistent123"
            val userId = "user123"

            every { eventRepository.findById(eventId) } returns null

            // Act & Assert
            assertThrows<ResourceNotFoundException> {
                eventService.deleteEvent(eventId, userId)
            }

            verify { eventRepository.findById(eventId) }
        }

        @Test
        @DisplayName("Should throw exception when non-creator tries to delete")
        fun testDeleteEvent_NotCreator_ThrowsException() {
            // Arrange
            val eventId = "event123"
            val creatorId = "user123"
            val otherUserId = "user456"
            val event = Event(
                title = "Test Event",
                description = "Test",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Test location",
                creatorUid = creatorId,
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = "IH-ABC12"
            )

            every { eventRepository.findById(eventId) } returns event

            // Act & Assert
            val exception = assertThrows<IllegalArgumentException> {
                eventService.deleteEvent(eventId, otherUserId)
            }

            assertEquals("Only the creator can delete this event", exception.message)
            
            // Verify delete was never called
            verify(exactly = 0) { eventRepository.delete(any()) }
            verify(exactly = 0) { eventUserRepository.deleteByEventId(any()) }
        }

        @Test
        @DisplayName("Should successfully delete event and related event_users when user is creator")
        fun testDeleteEvent_AsCreator_DeletesSuccessfully() {
            // Arrange
            val eventId = "event123"
            val creatorId = "user123"
            val event = Event(
                title = "Test Event",
                description = "Test",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Test location",
                creatorUid = creatorId,
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = "IH-ABC12"
            )

            every { eventRepository.findById(eventId) } returns event
            every { eventUserRepository.deleteByEventId(eventId) } returns 5 // 5 attendees deleted
            every { eventRepository.delete(eventId) } returns true

            // Act
            val deletedCount = eventService.deleteEvent(eventId, creatorId)

            // Assert
            assertEquals(5, deletedCount, "Should return count of deleted event_users")

            // Verify deletion order: event_users first, then event
            verifyOrder {
                eventUserRepository.deleteByEventId(eventId)
                eventRepository.delete(eventId)
            }
        }

        @Test
        @DisplayName("Should delete event even with no attendees")
        fun testDeleteEvent_NoAttendees_DeletesSuccessfully() {
            // Arrange
            val eventId = "event123"
            val creatorId = "user123"
            val event = Event(
                title = "Test Event",
                description = "Test",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Test location",
                creatorUid = creatorId,
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = "IH-ABC12"
            )

            every { eventRepository.findById(eventId) } returns event
            every { eventUserRepository.deleteByEventId(eventId) } returns 0 // No attendees
            every { eventRepository.delete(eventId) } returns true

            // Act
            val deletedCount = eventService.deleteEvent(eventId, creatorId)

            // Assert
            assertEquals(0, deletedCount)
            verify { eventRepository.delete(eventId) }
        }
    }

    /**
     * Tests for findEventByShareCode()
     */
    @Nested
    @DisplayName("Find Event By Share Code Tests")
    inner class FindEventByShareCodeTests {

        @Test
        @DisplayName("Should throw exception when share code doesn't exist")
        fun testFindByShareCode_NotFound_ThrowsException() {
            // Arrange
            val shareCode = "IH-ZZZZZ"
            val userId = "user123"

            every { eventRepository.findByShareCode(shareCode) } returns null

            // Act & Assert
            assertThrows<ResourceNotFoundException> {
                eventService.findEventByShareCode(shareCode, userId)
            }

            verify { eventRepository.findByShareCode(shareCode) }
        }

        @Test
        @DisplayName("Should return event with existing user status when user already joined")
        fun testFindByShareCode_ExistingUser_ReturnsWithStatus() {
            // Arrange
            val shareCode = "IH-ABC12"
            val eventId = "event123"
            val userId = "user456"
            val event = Event(
                title = "Test Event",
                description = "Test",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Test location",
                creatorUid = "user123",
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = shareCode
            )
            val eventUser = EventUser(
                eventId = eventId,
                userId = userId,
                status = EventUserStatus.ACCEPTED,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = "2024-01-02T12:00:00"
            )

            every { eventRepository.findByShareCode(shareCode) } returns Pair(eventId, event)
            every { eventUserRepository.findByEventIdAndUserId(eventId, userId) } returns Pair("eventUser123", eventUser)

            // Act
            val result = eventService.findEventByShareCode(shareCode, userId)

            // Assert
            assertNotNull(result)
            assertEquals(eventId, result?.get("id"))
            assertEquals(event, result?.get("event"))
            assertEquals(EventUserStatus.ACCEPTED, result?.get("userStatus"))
            assertEquals(EventUserRole.ATTENDEE, result?.get("userRole"))

            // Verify no new event_user was created
            verify(exactly = 0) { eventUserRepository.save(any()) }
        }

        @Test
        @DisplayName("Should create PENDING event_user when new user finds event via share code")
        fun testFindByShareCode_NewUser_CreatesPendingEventUser() {
            // Arrange
            val shareCode = "IH-ABC12"
            val eventId = "event123"
            val userId = "user456"
            val event = Event(
                title = "Test Event",
                description = "Test",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Test location",
                creatorUid = "user123", // Different from userId
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = shareCode
            )

            every { eventRepository.findByShareCode(shareCode) } returns Pair(eventId, event)
            every { eventUserRepository.findByEventIdAndUserId(eventId, userId) } returns null
            
            val eventUserSlot = slot<EventUser>()
            every { eventUserRepository.save(capture(eventUserSlot)) } returns Pair("eventUser456", mockk(relaxed = true))

            // Act
            val result = eventService.findEventByShareCode(shareCode, userId)

            // Assert
            assertNotNull(result)
            assertEquals(EventUserStatus.PENDING, result?.get("userStatus"))
            assertEquals(EventUserRole.ATTENDEE, result?.get("userRole"))

            // Verify new event_user was created with correct properties
            val capturedEventUser = eventUserSlot.captured
            assertEquals(eventId, capturedEventUser.eventId)
            assertEquals(userId, capturedEventUser.userId)
            assertEquals(EventUserStatus.PENDING, capturedEventUser.status)
            assertEquals(EventUserRole.ATTENDEE, capturedEventUser.role)
            assertNull(capturedEventUser.respondedAt, "New user hasn't responded yet")

            verify(exactly = 1) { eventUserRepository.save(any()) }
        }
    }

    /**
     * Tests for getEventById()
     */
    @Nested
    @DisplayName("Get Event By ID Tests")
    inner class GetEventByIdTests {

        @Test
        @DisplayName("Should throw exception when event doesn't exist")
        fun testGetEventById_NotFound_ThrowsException() {
            // Arrange
            val eventId = "nonexistent123"
            val userId = "user123"

            every { eventRepository.findById(eventId) } returns null

            // Act & Assert
            assertThrows<ResourceNotFoundException> {
                eventService.getEventById(eventId, userId)
            }
        }

        @Test
        @DisplayName("Should return event with user status when user is participant")
        fun testGetEventById_WithUserStatus_ReturnsCorrectly() {
            // Arrange
            val eventId = "event123"
            val userId = "user456"
            val event = Event(
                title = "Test Event",
                description = "Test",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Test location",
                creatorUid = "user123",
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = "IH-ABC12"
            )
            val eventUser = EventUser(
                eventId = eventId,
                userId = userId,
                status = EventUserStatus.ACCEPTED,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = "2024-01-02T12:00:00"
            )

            every { eventRepository.findById(eventId) } returns event
            every { eventUserRepository.findByEventIdAndUserId(eventId, userId) } returns Pair("eventUser123", eventUser)

            // Act
            val result = eventService.getEventById(eventId, userId)

            // Assert
            assertNotNull(result)
            assertEquals(eventId, result?.get("id"))
            assertEquals(event, result?.get("event"))
            assertEquals(EventUserStatus.ACCEPTED, result?.get("userStatus"))
            assertEquals(EventUserRole.ATTENDEE, result?.get("userRole"))
        }

        @Test
        @DisplayName("Should return event with null status when user is not participant")
        fun testGetEventById_NoUserStatus_ReturnsWithNulls() {
            // Arrange
            val eventId = "event123"
            val userId = "user456"
            val event = Event(
                title = "Test Event",
                description = "Test",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Test location",
                creatorUid = "user123",
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = "IH-ABC12"
            )

            every { eventRepository.findById(eventId) } returns event
            every { eventUserRepository.findByEventIdAndUserId(eventId, userId) } returns null

            // Act
            val result = eventService.getEventById(eventId, userId)

            // Assert
            assertNotNull(result)
            assertEquals(eventId, result?.get("id"))
            assertEquals(event, result?.get("event"))
            assertNull(result?.get("userStatus"))
            assertNull(result?.get("userRole"))
        }
    }
}
