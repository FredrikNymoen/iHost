package no.ntnu.prog2007.ihostapi.service.impl

import io.mockk.*
import no.ntnu.prog2007.ihostapi.exception.ResourceNotFoundException
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
 * Unit tests for EventUserServiceImpl
 *
 * AAA pattern: Arrange, Act, Assert
 *
 * Mockk is used for mocking dependencies
 *
 * Testing following cases:
 * - Test authorization for inviting users (only creator can invite)
 * - Test duplicate invitation prevention
 * - Test invitation acceptance/decline authorization
 * - Test status transitions and timestamp updates
 * - Test event attendee retrieval with optional filtering
 * - Test user's events retrieval with status filtering
 */
@DisplayName("EventUserServiceImpl Tests")
class EventUserServiceImplTest {
    // Mocks for dependencies
    private lateinit var eventUserRepository: EventUserRepository
    private lateinit var eventRepository: EventRepository

    // Service under test
    private lateinit var eventUserService: EventUserServiceImpl

    @BeforeEach
    fun setup() {
        // Initialize mocks
        eventUserRepository = mockk()
        eventRepository = mockk()

        // Initialize service with mocked dependencies
        eventUserService = EventUserServiceImpl(eventUserRepository, eventRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks() // Clear mocks after each test
    }

    /**
     * Tests for inviteUsers()
     */
    @Nested
    @DisplayName("Invite Users Tests")
    inner class InviteUsersTests {

        @Test
        @DisplayName("Should throw exception when event doesn't exist")
        fun testInviteUsers_EventNotFound_ThrowsException() {
            // Arrange
            val eventId = "nonexistent123"
            val creatorId = "user123"
            val userIds = listOf("user456", "user789")

            every { eventRepository.findById(eventId) } returns null

            // Act & Assert
            assertThrows<ResourceNotFoundException> {
                eventUserService.inviteUsers(eventId, userIds, creatorId)
            }

            verify { eventRepository.findById(eventId) }
            verify(exactly = 0) { eventUserRepository.save(any()) }
        }

        @Test
        @DisplayName("Should throw exception when non-creator tries to invite users")
        fun testInviteUsers_NotCreator_ThrowsException() {
            // Arrange
            val eventId = "event123"
            val creatorId = "user123"
            val otherUserId = "user456"
            val userIds = listOf("user789")
            val event = Event(
                title = "Test Event",
                description = "Test",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Test location",
                creatorUid = creatorId, // Different from otherUserId
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = "IH-ABC12"
            )

            every { eventRepository.findById(eventId) } returns event

            // Act & Assert
            val exception = assertThrows<IllegalArgumentException> {
                eventUserService.inviteUsers(eventId, userIds, otherUserId)
            }

            assertEquals("Only the event creator can invite users", exception.message)
            verify(exactly = 0) { eventUserRepository.save(any()) }
        }

        @Test
        @DisplayName("Should successfully invite new users with PENDING status and ATTENDEE role")
        fun testInviteUsers_ValidRequest_InvitesSuccessfully() {
            // Arrange
            val eventId = "event123"
            val creatorId = "user123"
            val userIds = listOf("user456", "user789")
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

            // Mock that no existing invites exist
            every { eventUserRepository.findByEventIdAndUserId(eventId, any()) } returns null

            // Capture EventUser objects being saved
            val savedEventUsers = mutableListOf<EventUser>()
            every { eventUserRepository.save(capture(savedEventUsers)) } answers {
                val savedEventUser = savedEventUsers.last()
                Pair("eventUser${savedEventUsers.size}", savedEventUser)
            }

            // Act
            val result = eventUserService.inviteUsers(eventId, userIds, creatorId)

            // Assert
            assertEquals(2, result.size, "Should invite 2 users")

            // Verify all saved EventUsers have correct properties
            savedEventUsers.forEachIndexed { _, eventUser ->
                assertEquals(eventId, eventUser.eventId)
                assertTrue(userIds.contains(eventUser.userId), "userId should be in the invited list")
                assertEquals(EventUserStatus.PENDING, eventUser.status, "New invites should have PENDING status")
                assertEquals(EventUserRole.ATTENDEE, eventUser.role, "New invites should have ATTENDEE role")
                assertNotNull(eventUser.invitedAt, "invitedAt should be set")
                assertNull(eventUser.respondedAt, "New invites should not have respondedAt set")
            }

            // Verify repository interactions
            verify(exactly = 2) { eventUserRepository.findByEventIdAndUserId(eventId, any()) }
            verify(exactly = 2) { eventUserRepository.save(any()) }
        }

        @Test
        @DisplayName("Should skip already invited users and only invite new ones")
        fun testInviteUsers_SkipsExistingInvites() {
            // Arrange
            val eventId = "event123"
            val creatorId = "user123"
            val userIds = listOf("user456", "user789", "user101")
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

            val existingEventUser = EventUser(
                eventId = eventId,
                userId = "user456",
                status = EventUserStatus.PENDING,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )

            every { eventRepository.findById(eventId) } returns event

            // user456 already invited, user789 and user101 are new
            every { eventUserRepository.findByEventIdAndUserId(eventId, "user456") } returns Pair("eventUser1", existingEventUser)
            every { eventUserRepository.findByEventIdAndUserId(eventId, "user789") } returns null
            every { eventUserRepository.findByEventIdAndUserId(eventId, "user101") } returns null

            every { eventUserRepository.save(any()) } answers {
                Pair("eventUser${Math.random()}", mockk(relaxed = true))
            }

            // Act
            val result = eventUserService.inviteUsers(eventId, userIds, creatorId)

            // Assert
            assertEquals(2, result.size, "Should only invite 2 new users (skip the existing one)")

            // Verify save was only called twice (for user789 and user101, not user456)
            verify(exactly = 2) { eventUserRepository.save(any()) }
        }

        @Test
        @DisplayName("Should return empty list when all users are already invited")
        fun testInviteUsers_AllUsersAlreadyInvited_ReturnsEmptyList() {
            // Arrange
            val eventId = "event123"
            val creatorId = "user123"
            val userIds = listOf("user456", "user789")
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

            // Both users already invited
            every { eventUserRepository.findByEventIdAndUserId(eventId, any()) } returns Pair("eventUser1", mockk(relaxed = true))

            // Act
            val result = eventUserService.inviteUsers(eventId, userIds, creatorId)

            // Assert
            assertEquals(0, result.size, "Should return empty list when all users already invited")
            verify(exactly = 0) { eventUserRepository.save(any()) }
        }
    }

    /**
     * Tests for acceptInvitation()
     */
    @Nested
    @DisplayName("Accept Invitation Tests")
    inner class AcceptInvitationTests {

        @Test
        @DisplayName("Should throw exception when invitation doesn't exist")
        fun testAcceptInvitation_NotFound_ThrowsException() {
            // Arrange
            val eventUserId = "nonexistent123"
            val userId = "user456"

            every { eventUserRepository.findById(eventUserId) } returns null

            // Act & Assert
            assertThrows<ResourceNotFoundException> {
                eventUserService.acceptInvitation(eventUserId, userId)
            }

            verify { eventUserRepository.findById(eventUserId) }
            verify(exactly = 0) { eventUserRepository.update(any(), any()) }
        }

        @Test
        @DisplayName("Should throw exception when user tries to accept someone else's invitation")
        fun testAcceptInvitation_WrongUser_ThrowsException() {
            // Arrange
            val eventUserId = "eventUser123"
            val wrongUserId = "user999"
            val eventUser = EventUser(
                eventId = "event123",
                userId = "user456", // The actual invited user
                status = EventUserStatus.PENDING,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )

            every { eventUserRepository.findById(eventUserId) } returns eventUser

            // Act & Assert
            val exception = assertThrows<IllegalArgumentException> {
                eventUserService.acceptInvitation(eventUserId, wrongUserId)
            }

            assertEquals("You can only respond to your own invitations", exception.message)
            verify(exactly = 0) { eventUserRepository.update(any(), any()) }
        }

        @Test
        @DisplayName("Should successfully accept invitation and update status to ACCEPTED")
        fun testAcceptInvitation_ValidRequest_AcceptsSuccessfully() {
            // Arrange
            val eventUserId = "eventUser123"
            val userId = "user456"
            val eventId = "event123"
            val eventUser = EventUser(
                eventId = eventId,
                userId = userId,
                status = EventUserStatus.PENDING,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )

            every { eventUserRepository.findById(eventUserId) } returns eventUser

            // Capture the update data
            val updateSlot = slot<Map<String, Any>>()
            every { eventUserRepository.update(eventUserId, capture(updateSlot)) } returns true

            // Act
            val returnedEventId = eventUserService.acceptInvitation(eventUserId, userId)

            // Assert
            assertEquals(eventId, returnedEventId, "Should return the eventId")

            val capturedUpdate = updateSlot.captured
            assertEquals(EventUserStatus.ACCEPTED.name, capturedUpdate["status"], "Status should be updated to ACCEPTED")
            assertNotNull(capturedUpdate["respondedAt"], "respondedAt should be set when accepting")

            verify(exactly = 1) { eventUserRepository.update(eventUserId, any()) }
        }
    }

    /**
     * Tests for declineInvitation()
     */
    @Nested
    @DisplayName("Decline Invitation Tests")
    inner class DeclineInvitationTests {

        @Test
        @DisplayName("Should throw exception when invitation doesn't exist")
        fun testDeclineInvitation_NotFound_ThrowsException() {
            // Arrange
            val eventUserId = "nonexistent123"
            val userId = "user456"

            every { eventUserRepository.findById(eventUserId) } returns null

            // Act & Assert
            assertThrows<ResourceNotFoundException> {
                eventUserService.declineInvitation(eventUserId, userId)
            }

            verify { eventUserRepository.findById(eventUserId) }
            verify(exactly = 0) { eventUserRepository.update(any(), any()) }
        }

        @Test
        @DisplayName("Should throw exception when user tries to decline someone else's invitation")
        fun testDeclineInvitation_WrongUser_ThrowsException() {
            // Arrange
            val eventUserId = "eventUser123"
            val wrongUserId = "user999"
            val eventUser = EventUser(
                eventId = "event123",
                userId = "user456", // The actual invited user
                status = EventUserStatus.PENDING,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )

            every { eventUserRepository.findById(eventUserId) } returns eventUser

            // Act & Assert
            val exception = assertThrows<IllegalArgumentException> {
                eventUserService.declineInvitation(eventUserId, wrongUserId)
            }

            assertEquals("You can only respond to your own invitations", exception.message)
            verify(exactly = 0) { eventUserRepository.update(any(), any()) }
        }

        @Test
        @DisplayName("Should successfully decline invitation and update status to DECLINED")
        fun testDeclineInvitation_ValidRequest_DeclinesSuccessfully() {
            // Arrange
            val eventUserId = "eventUser123"
            val userId = "user456"
            val eventId = "event123"
            val eventUser = EventUser(
                eventId = eventId,
                userId = userId,
                status = EventUserStatus.PENDING,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )

            every { eventUserRepository.findById(eventUserId) } returns eventUser

            // Capture the update data
            val updateSlot = slot<Map<String, Any>>()
            every { eventUserRepository.update(eventUserId, capture(updateSlot)) } returns true

            // Act
            val returnedEventId = eventUserService.declineInvitation(eventUserId, userId)

            // Assert
            assertEquals(eventId, returnedEventId, "Should return the eventId")

            val capturedUpdate = updateSlot.captured
            assertEquals(EventUserStatus.DECLINED.name, capturedUpdate["status"], "Status should be updated to DECLINED")
            assertNotNull(capturedUpdate["respondedAt"], "respondedAt should be set when declining")

            verify(exactly = 1) { eventUserRepository.update(eventUserId, any()) }
        }
    }

    /**
     * Tests for getEventAttendees()
     */
    @Nested
    @DisplayName("Get Event Attendees Tests")
    inner class GetEventAttendeesTests {

        @Test
        @DisplayName("Should return all attendees when no status filter provided")
        fun testGetEventAttendees_NoFilter_ReturnsAllAttendees() {
            // Arrange
            val eventId = "event123"
            val eventUser1 = EventUser(
                eventId = eventId,
                userId = "user456",
                status = EventUserStatus.ACCEPTED,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = "2024-01-02T12:00:00"
            )
            val eventUser2 = EventUser(
                eventId = eventId,
                userId = "user789",
                status = EventUserStatus.PENDING,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )

            every { eventUserRepository.findByEventId(eventId) } returns listOf(
                Pair("eventUser1", eventUser1),
                Pair("eventUser2", eventUser2)
            )

            // Act
            val result = eventUserService.getEventAttendees(eventId, null)

            // Assert
            assertEquals(2, result.size, "Should return all attendees regardless of status")
            verify(exactly = 1) { eventUserRepository.findByEventId(eventId) }
            verify(exactly = 0) { eventUserRepository.findByEventIdAndStatus(any(), any()) }
        }

        @Test
        @DisplayName("Should return only ACCEPTED attendees when filtered by ACCEPTED status")
        fun testGetEventAttendees_AcceptedFilter_ReturnsOnlyAccepted() {
            // Arrange
            val eventId = "event123"
            val eventUser1 = EventUser(
                eventId = eventId,
                userId = "user456",
                status = EventUserStatus.ACCEPTED,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = "2024-01-02T12:00:00"
            )

            every { eventUserRepository.findByEventIdAndStatus(eventId, "ACCEPTED") } returns listOf(
                Pair("eventUser1", eventUser1)
            )

            // Act
            val result = eventUserService.getEventAttendees(eventId, "ACCEPTED")

            // Assert
            assertEquals(1, result.size, "Should return only ACCEPTED attendees")
            verify(exactly = 1) { eventUserRepository.findByEventIdAndStatus(eventId, "ACCEPTED") }
            verify(exactly = 0) { eventUserRepository.findByEventId(any()) }
        }

        @Test
        @DisplayName("Should return only PENDING attendees when filtered by PENDING status")
        fun testGetEventAttendees_PendingFilter_ReturnsOnlyPending() {
            // Arrange
            val eventId = "event123"
            val eventUser1 = EventUser(
                eventId = eventId,
                userId = "user789",
                status = EventUserStatus.PENDING,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )
            val eventUser2 = EventUser(
                eventId = eventId,
                userId = "user101",
                status = EventUserStatus.PENDING,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T13:00:00",
                respondedAt = null
            )

            every { eventUserRepository.findByEventIdAndStatus(eventId, "PENDING") } returns listOf(
                Pair("eventUser1", eventUser1),
                Pair("eventUser2", eventUser2)
            )

            // Act
            val result = eventUserService.getEventAttendees(eventId, "PENDING")

            // Assert
            assertEquals(2, result.size, "Should return all PENDING attendees")
            verify(exactly = 1) { eventUserRepository.findByEventIdAndStatus(eventId, "PENDING") }
        }

        @Test
        @DisplayName("Should return empty list when no attendees exist")
        fun testGetEventAttendees_NoAttendees_ReturnsEmptyList() {
            // Arrange
            val eventId = "event123"

            every { eventUserRepository.findByEventId(eventId) } returns emptyList()

            // Act
            val result = eventUserService.getEventAttendees(eventId, null)

            // Assert
            assertEquals(0, result.size, "Should return empty list when no attendees")
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("Should return empty list when no attendees match the status filter")
        fun testGetEventAttendees_NoMatchingStatus_ReturnsEmptyList() {
            // Arrange
            val eventId = "event123"

            every { eventUserRepository.findByEventIdAndStatus(eventId, "DECLINED") } returns emptyList()

            // Act
            val result = eventUserService.getEventAttendees(eventId, "DECLINED")

            // Assert
            assertEquals(0, result.size, "Should return empty list when no DECLINED attendees")
        }
    }

    /**
     * Tests for getMyEvents()
     */
    @Nested
    @DisplayName("Get My Events Tests")
    inner class GetMyEventsTests {

        @Test
        @DisplayName("Should return all events for user when no status filter provided")
        fun testGetMyEvents_NoFilter_ReturnsAllEvents() {
            // Arrange
            val userId = "user456"
            val eventId1 = "event123"
            val eventId2 = "event456"
            val eventUser1 = EventUser(
                eventId = eventId1,
                userId = userId,
                status = EventUserStatus.ACCEPTED,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = "2024-01-02T12:00:00"
            )
            val eventUser2 = EventUser(
                eventId = eventId2,
                userId = userId,
                status = EventUserStatus.PENDING,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T13:00:00",
                respondedAt = null
            )
            val event1 = Event(
                title = "Event 1",
                description = "Test event 1",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Location 1",
                creatorUid = "user123",
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = "IH-ABC12"
            )
            val event2 = Event(
                title = "Event 2",
                description = "Test event 2",
                eventDate = "2024-07-20",
                eventTime = "19:00",
                location = "Location 2",
                creatorUid = "user123",
                free = false,
                price = 50.0,
                createdAt = "2024-01-01T13:00:00",
                updatedAt = "2024-01-01T13:00:00",
                shareCode = "IH-XYZ89"
            )

            every { eventUserRepository.findByUserId(userId) } returns listOf(
                Pair("eventUser1", eventUser1),
                Pair("eventUser2", eventUser2)
            )
            every { eventRepository.findById(eventId1) } returns event1
            every { eventRepository.findById(eventId2) } returns event2

            // Act
            val result = eventUserService.getMyEvents(userId, null)

            // Assert
            assertEquals(2, result.size, "Should return all events for user")

            // Verify first event
            val firstEventData = result[0]
            assertEquals(eventId1, firstEventData["id"])
            assertEquals(event1, firstEventData["event"])
            assertEquals(EventUserStatus.ACCEPTED, firstEventData["userStatus"])
            assertEquals(EventUserRole.ATTENDEE, firstEventData["userRole"])

            // Verify second event
            val secondEventData = result[1]
            assertEquals(eventId2, secondEventData["id"])
            assertEquals(event2, secondEventData["event"])
            assertEquals(EventUserStatus.PENDING, secondEventData["userStatus"])
            assertEquals(EventUserRole.ATTENDEE, secondEventData["userRole"])

            verify(exactly = 1) { eventUserRepository.findByUserId(userId) }
            verify(exactly = 0) { eventUserRepository.findByUserIdAndStatus(any(), any()) }
        }

        @Test
        @DisplayName("Should return only accepted events when filtered by ACCEPTED status")
        fun testGetMyEvents_AcceptedFilter_ReturnsOnlyAccepted() {
            // Arrange
            val userId = "user456"
            val eventId = "event123"
            val eventUser = EventUser(
                eventId = eventId,
                userId = userId,
                status = EventUserStatus.ACCEPTED,
                role = EventUserRole.CREATOR,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = "2024-01-02T12:00:00"
            )
            val event = Event(
                title = "My Event",
                description = "Test event",
                eventDate = "2024-07-15",
                eventTime = "18:00",
                location = "Test location",
                creatorUid = userId,
                free = true,
                price = 0.0,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                shareCode = "IH-ABC12"
            )

            every { eventUserRepository.findByUserIdAndStatus(userId, "ACCEPTED") } returns listOf(
                Pair("eventUser1", eventUser)
            )
            every { eventRepository.findById(eventId) } returns event

            // Act
            val result = eventUserService.getMyEvents(userId, "ACCEPTED")

            // Assert
            assertEquals(1, result.size, "Should return only ACCEPTED events")
            val eventData = result[0]
            assertEquals(EventUserStatus.ACCEPTED, eventData["userStatus"])
            assertEquals(EventUserRole.CREATOR, eventData["userRole"])

            verify(exactly = 1) { eventUserRepository.findByUserIdAndStatus(userId, "ACCEPTED") }
            verify(exactly = 0) { eventUserRepository.findByUserId(any()) }
        }

        @Test
        @DisplayName("Should return only pending events when filtered by PENDING status")
        fun testGetMyEvents_PendingFilter_ReturnsOnlyPending() {
            // Arrange
            val userId = "user456"
            val eventId = "event123"
            val eventUser = EventUser(
                eventId = eventId,
                userId = userId,
                status = EventUserStatus.PENDING,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )
            val event = Event(
                title = "Pending Event",
                description = "Test event",
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

            every { eventUserRepository.findByUserIdAndStatus(userId, "PENDING") } returns listOf(
                Pair("eventUser1", eventUser)
            )
            every { eventRepository.findById(eventId) } returns event

            // Act
            val result = eventUserService.getMyEvents(userId, "PENDING")

            // Assert
            assertEquals(1, result.size, "Should return only PENDING events")
            assertEquals(EventUserStatus.PENDING, result[0]["userStatus"])
        }

        @Test
        @DisplayName("Should handle missing events gracefully and exclude them from results")
        fun testGetMyEvents_MissingEvent_ExcludesFromResults() {
            // Arrange
            val userId = "user456"
            val eventId1 = "event123"
            val eventId2 = "event456" // This event will not be found
            val eventUser1 = EventUser(
                eventId = eventId1,
                userId = userId,
                status = EventUserStatus.ACCEPTED,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = "2024-01-02T12:00:00"
            )
            val eventUser2 = EventUser(
                eventId = eventId2,
                userId = userId,
                status = EventUserStatus.ACCEPTED,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T13:00:00",
                respondedAt = "2024-01-02T13:00:00"
            )
            val event1 = Event(
                title = "Event 1",
                description = "Test event",
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

            every { eventUserRepository.findByUserId(userId) } returns listOf(
                Pair("eventUser1", eventUser1),
                Pair("eventUser2", eventUser2)
            )
            every { eventRepository.findById(eventId1) } returns event1
            every { eventRepository.findById(eventId2) } returns null // Event not found

            // Act
            val result = eventUserService.getMyEvents(userId, null)

            // Assert
            assertEquals(1, result.size, "Should exclude missing events and return only found events")
            assertEquals(eventId1, result[0]["id"])
            assertEquals(event1, result[0]["event"])
        }

        @Test
        @DisplayName("Should handle exception when fetching event and exclude from results")
        fun testGetMyEvents_EventFetchException_ExcludesFromResults() {
            // Arrange
            val userId = "user456"
            val eventId1 = "event123"
            val eventId2 = "event456"
            val eventUser1 = EventUser(
                eventId = eventId1,
                userId = userId,
                status = EventUserStatus.ACCEPTED,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T12:00:00",
                respondedAt = "2024-01-02T12:00:00"
            )
            val eventUser2 = EventUser(
                eventId = eventId2,
                userId = userId,
                status = EventUserStatus.ACCEPTED,
                role = EventUserRole.ATTENDEE,
                invitedAt = "2024-01-01T13:00:00",
                respondedAt = "2024-01-02T13:00:00"
            )
            val event1 = Event(
                title = "Event 1",
                description = "Test event",
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

            every { eventUserRepository.findByUserId(userId) } returns listOf(
                Pair("eventUser1", eventUser1),
                Pair("eventUser2", eventUser2)
            )
            every { eventRepository.findById(eventId1) } returns event1
            every { eventRepository.findById(eventId2) } throws RuntimeException("Database error")

            // Act
            val result = eventUserService.getMyEvents(userId, null)

            // Assert
            assertEquals(1, result.size, "Should handle exceptions gracefully and exclude failed events")
            assertEquals(eventId1, result[0]["id"])
        }

        @Test
        @DisplayName("Should return empty list when user has no events")
        fun testGetMyEvents_NoEvents_ReturnsEmptyList() {
            // Arrange
            val userId = "user456"

            every { eventUserRepository.findByUserId(userId) } returns emptyList()

            // Act
            val result = eventUserService.getMyEvents(userId, null)

            // Assert
            assertEquals(0, result.size, "Should return empty list when user has no events")
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("Should return empty list when no events match the status filter")
        fun testGetMyEvents_NoMatchingStatus_ReturnsEmptyList() {
            // Arrange
            val userId = "user456"

            every { eventUserRepository.findByUserIdAndStatus(userId, "DECLINED") } returns emptyList()

            // Act
            val result = eventUserService.getMyEvents(userId, "DECLINED")

            // Assert
            assertEquals(0, result.size, "Should return empty list when no events match status")
        }
    }
}
