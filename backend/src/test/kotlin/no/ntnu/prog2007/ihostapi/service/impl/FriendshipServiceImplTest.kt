package no.ntnu.prog2007.ihostapi.service.impl

import io.mockk.*
import no.ntnu.prog2007.ihostapi.exception.ResourceNotFoundException
import no.ntnu.prog2007.ihostapi.model.entity.Friendship
import no.ntnu.prog2007.ihostapi.repository.FriendshipRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for FriendshipServiceImpl
 *
 * AAA pattern: Arrange, Act, Assert
 *
 * Mockk is used for mocking dependencies
 *
 * Testing following cases:
 * - Test business logic validations (self-friendship, duplicates)
 * - Test authorization rules (only recipient can accept/decline)
 * - Test state transitions (PENDING → ACCEPTED/DECLINED)
 */
@DisplayName("FriendshipServiceImpl Tests")
class FriendshipServiceImplTest {

    // Mock for repository dependency
    private lateinit var friendshipRepository: FriendshipRepository

    // Service under test
    private lateinit var friendshipService: FriendshipServiceImpl

    @BeforeEach
    fun setup() {
        // Initialize mock
        friendshipRepository = mockk()

        // Initialize service with mocked dependency (no Firestore anymore!)
        friendshipService = FriendshipServiceImpl(friendshipRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks() // Clear mocks after each test
    }

    /**
     * Tests for sendFriendRequest()
     */
    @Nested
    @DisplayName("Send Friend Request Tests")
    inner class SendFriendRequestTests {

        @Test
        @DisplayName("Should throw exception when trying to send request to yourself")
        fun testSendFriendRequest_ToSelf_ThrowsException() {
            // Arrange
            val userId = "user123"

            // Act & Assert
            val exception = assertThrows<IllegalArgumentException> {
                friendshipService.sendFriendRequest(userId, userId)
            }

            assertEquals("Cannot send friend request to yourself", exception.message)
            
            // Verify no repository calls were made
            verify(exactly = 0) { friendshipRepository.findByUsers(any(), any()) }
            verify(exactly = 0) { friendshipRepository.save(any(), any(), any(), any(), any(), any()) }
        }

        @Test
        @DisplayName("Should throw exception when friendship already exists")
        fun testSendFriendRequest_DuplicateRequest_ThrowsException() {
            // Arrange
            val fromUser = "user123"
            val toUser = "user456"
            
            // Mock that a friendship already exists
            val existingFriendship = Friendship(
                id = "friendship789",
                user1Id = fromUser,
                user2Id = toUser,
                status = "PENDING",
                requestedBy = fromUser,
                requestedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )

            every { friendshipRepository.findByUsers(fromUser, toUser) } returns existingFriendship

            // Act & Assert
            val exception = assertThrows<IllegalArgumentException> {
                friendshipService.sendFriendRequest(fromUser, toUser)
            }

            assertEquals("Friendship request already exists", exception.message)
            
            // Verify we checked for existing friendship but didn't create a new one
            verify(exactly = 1) { friendshipRepository.findByUsers(fromUser, toUser) }
            verify(exactly = 0) { friendshipRepository.save(any(), any(), any(), any(), any(), any()) }
        }

        @Test
        @DisplayName("Should successfully create friend request when validation passes")
        fun testSendFriendRequest_ValidRequest_CreatesSuccessfully() {
            // Arrange
            val fromUser = "user123"
            val toUser = "user456"
            val savedFriendship = Friendship(
                id = "friendship789",
                user1Id = fromUser,
                user2Id = toUser,
                status = "PENDING",
                requestedBy = fromUser,
                requestedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )

            // Mock that no existing friendship exists
            every { friendshipRepository.findByUsers(fromUser, toUser) } returns null
            
            // Mock the repository save method
            every { 
                friendshipRepository.save(
                    user1Id = fromUser,
                    user2Id = toUser,
                    status = "PENDING",
                    requestedBy = fromUser,
                    requestedAt = any(),
                    respondedAt = null
                )
            } returns savedFriendship

            // Act
            val result = friendshipService.sendFriendRequest(fromUser, toUser)

            // Assert
            assertNotNull(result)
            assertEquals(savedFriendship.id, result.id)
            assertEquals(fromUser, result.user1Id)
            assertEquals(toUser, result.user2Id)
            assertEquals("PENDING", result.status)
            assertEquals(fromUser, result.requestedBy)
            assertNotNull(result.requestedAt)
            assertNull(result.respondedAt)

            // Verify repository interactions
            verify(exactly = 1) { friendshipRepository.findByUsers(fromUser, toUser) }
            verify(exactly = 1) { friendshipRepository.save(any(), any(), any(), any(), any(), any()) }
        }
    }

    /**
     * Tests for acceptFriendRequest()
     */
    @Nested
    @DisplayName("Accept Friend Request Tests")
    inner class AcceptFriendRequestTests {

        @Test
        @DisplayName("Should throw exception when friendship doesn't exist")
        fun testAcceptFriendRequest_NotFound_ThrowsException() {
            // Arrange
            val friendshipId = "nonexistent123"
            val userId = "user123"

            every { friendshipRepository.findById(friendshipId) } returns null // Friendship not found

            // Act & Assert
            assertThrows<ResourceNotFoundException> {
                friendshipService.acceptFriendRequest(friendshipId, userId)
            }

            verify { friendshipRepository.findById(friendshipId) }
        }

        @Test
        @DisplayName("Should throw exception when user is not the recipient")
        fun testAcceptFriendRequest_WrongUser_ThrowsException() {
            // Arrange
            val friendshipId = "friendship123"
            val wrongUserId = "user999"
            val friendship = Friendship(
                id = friendshipId,
                user1Id = "user123",
                user2Id = "user456", // The actual recipient
                status = "PENDING",
                requestedBy = "user123",
                requestedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )

            every { friendshipRepository.findById(friendshipId) } returns friendship

            // Act & Assert
            val exception = assertThrows<IllegalArgumentException> {
                friendshipService.acceptFriendRequest(friendshipId, wrongUserId)
            }

            assertEquals("You can only accept requests sent to you", exception.message)
            
            // Verify update was never called
            verify(exactly = 0) { friendshipRepository.update(any()) }
        }

        @Test
        @DisplayName("Should throw exception when friendship is not pending")
        fun testAcceptFriendRequest_AlreadyAccepted_ThrowsException() {
            // Arrange
            val friendshipId = "friendship123"
            val userId = "user456"
            val friendship = Friendship(
                id = friendshipId,
                user1Id = "user123",
                user2Id = userId,
                status = "ACCEPTED", // Already accepted
                requestedBy = "user123",
                requestedAt = "2024-01-01T12:00:00",
                respondedAt = "2024-01-02T12:00:00"
            )

            every { friendshipRepository.findById(friendshipId) } returns friendship

            // Act & Assert
            val exception = assertThrows<IllegalArgumentException> {
                friendshipService.acceptFriendRequest(friendshipId, userId)
            }

            assertEquals("Friendship is not pending", exception.message)
            verify(exactly = 0) { friendshipRepository.update(any()) }
        }

        @Test
        @DisplayName("Should successfully accept friend request when validation passes")
        fun testAcceptFriendRequest_ValidRequest_AcceptsSuccessfully() {
            // Arrange
            val friendshipId = "friendship123"
            val recipientId = "user456"
            val friendship = Friendship(
                id = friendshipId,
                user1Id = "user123",
                user2Id = recipientId,
                status = "PENDING",
                requestedBy = "user123",
                requestedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )

            every { friendshipRepository.findById(friendshipId) } returns friendship
            every { friendshipRepository.update(any()) } returns true // Mock successful update

            // Act
            val result = friendshipService.acceptFriendRequest(friendshipId, recipientId)

            // Assert
            assertNotNull(result)
            assertEquals(friendshipId, result.id)
            assertEquals("ACCEPTED", result.status)
            assertNotNull(result.respondedAt, "respondedAt should be set when accepting")

            // Verify repository update was called
            verify(exactly = 1) { friendshipRepository.update(any()) }
        }
    }

    /**
     * Tests for declineFriendRequest()
     */
    @Nested
    @DisplayName("Decline Friend Request Tests")
    inner class DeclineFriendRequestTests {

        @Test
        @DisplayName("Should throw exception when user is not the recipient")
        fun testDeclineFriendRequest_WrongUser_ThrowsException() {
            // Arrange
            val friendshipId = "friendship123"
            val wrongUserId = "user999"
            val friendship = Friendship(
                id = friendshipId,
                user1Id = "user123",
                user2Id = "user456",
                status = "PENDING",
                requestedBy = "user123",
                requestedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )

            every { friendshipRepository.findById(friendshipId) } returns friendship

            // Act & Assert
            val exception = assertThrows<IllegalArgumentException> {
                friendshipService.declineFriendRequest(friendshipId, wrongUserId)
            }

            assertEquals("You can only decline requests sent to you", exception.message)
            verify(exactly = 0) { friendshipRepository.update(any()) }
        }

        @Test
        @DisplayName("Should successfully decline friend request")
        fun testDeclineFriendRequest_ValidRequest_DeclinesSuccessfully() {
            // Arrange
            val friendshipId = "friendship123"
            val recipientId = "user456"
            val friendship = Friendship(
                id = friendshipId,
                user1Id = "user123",
                user2Id = recipientId,
                status = "PENDING",
                requestedBy = "user123",
                requestedAt = "2024-01-01T12:00:00",
                respondedAt = null
            )

            every { friendshipRepository.findById(friendshipId) } returns friendship
            every { friendshipRepository.update(any()) } returns true

            // Act
            val result = friendshipService.declineFriendRequest(friendshipId, recipientId)

            // Assert
            assertNotNull(result)
            assertEquals(friendshipId, result.id)
            assertEquals("DECLINED", result.status)
            assertNotNull(result.respondedAt)

            verify(exactly = 1) { friendshipRepository.update(any()) }
        }
    }

    /**
     * Tests for removeFriend()
     */
    @Nested
    @DisplayName("Remove Friend Tests")
    inner class RemoveFriendTests {

        @Test
        @DisplayName("Should throw exception when friendship doesn't exist")
        fun testRemoveFriend_NotFound_ThrowsException() {
            // Arrange
            val friendshipId = "nonexistent123"
            val userId = "user123"

            every { friendshipRepository.findById(friendshipId) } returns null

            // Act & Assert
            assertThrows<ResourceNotFoundException> {
                friendshipService.removeFriend(friendshipId, userId)
            }
        }

        @Test
        @DisplayName("Should throw exception when user is not part of the friendship")
        fun testRemoveFriend_NotParticipant_ThrowsException() {
            // Arrange
            val friendshipId = "friendship123"
            val outsiderUserId = "user999"
            val friendship = Friendship(
                id = friendshipId,
                user1Id = "user123",
                user2Id = "user456",
                status = "ACCEPTED",
                requestedBy = "user123",
                requestedAt = "2024-01-01T12:00:00",
                respondedAt = "2024-01-02T12:00:00"
            )

            every { friendshipRepository.findById(friendshipId) } returns friendship

            // Act & Assert
            val exception = assertThrows<IllegalArgumentException> {
                friendshipService.removeFriend(friendshipId, outsiderUserId)
            }

            assertEquals("You can only remove your own friendships", exception.message)
            verify(exactly = 0) { friendshipRepository.delete(any()) }
        }

        @Test
        @DisplayName("Should successfully remove friend when user is user1")
        fun testRemoveFriend_AsUser1_RemovesSuccessfully() {
            // Arrange
            val friendshipId = "friendship123"
            val userId = "user123" // user1
            val friendship = Friendship(
                id = friendshipId,
                user1Id = userId,
                user2Id = "user456",
                status = "ACCEPTED",
                requestedBy = userId,
                requestedAt = "2024-01-01T12:00:00",
                respondedAt = "2024-01-02T12:00:00"
            )

            every { friendshipRepository.findById(friendshipId) } returns friendship
            every { friendshipRepository.delete(friendshipId) } returns true

            // Act
            friendshipService.removeFriend(friendshipId, userId)

            // Assert - no exception thrown means success
            verify(exactly = 1) { friendshipRepository.delete(friendshipId) }
        }

        @Test
        @DisplayName("Should successfully remove friend when user is user2")
        fun testRemoveFriend_AsUser2_RemovesSuccessfully() {
            // Arrange
            val friendshipId = "friendship123"
            val userId = "user456" // user2
            val friendship = Friendship(
                id = friendshipId,
                user1Id = "user123",
                user2Id = userId,
                status = "ACCEPTED",
                requestedBy = "user123",
                requestedAt = "2024-01-01T12:00:00",
                respondedAt = "2024-01-02T12:00:00"
            )

            every { friendshipRepository.findById(friendshipId) } returns friendship
            every { friendshipRepository.delete(friendshipId) } returns true

            // Act
            friendshipService.removeFriend(friendshipId, userId)

            // Assert
            verify(exactly = 1) { friendshipRepository.delete(friendshipId) }
        }
    }
}
