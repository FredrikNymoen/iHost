package no.ntnu.prog2007.ihostapi.service.impl

import com.google.cloud.firestore.Firestore
import com.google.firebase.auth.FirebaseAuth
import no.ntnu.prog2007.ihostapi.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import io.mockk.*
import no.ntnu.prog2007.ihostapi.model.entity.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit testing for UserServiceImpl
 *
 * AAA pattern: Arrange, Act, Assert
 *
 * Mockk is used for mocking dependencies
 *
 * Testing following cases:
 * - Valid and available
 * - Already exists
 * - Too short
 * - Too long
 * - Boundary conditions (exactly min and max length)
 * - Edge cases (1 character, empty string)
 */

@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {
    // Mocks for dependendencies
    private lateinit var userRepository: UserRepository
    private lateinit var firebaseAuth: FirebaseAuth

    // Service under test
    private lateinit var userService: UserServiceImpl

    @BeforeEach
    fun setup() {
        // Initialize mocks
        userRepository = mockk()
        firebaseAuth = mockk()

        // Initialize service with mocked dependencies
        userService = UserServiceImpl(
            userRepository,
            firebaseAuth
        )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks() // Clear mocks after each test
    }

    /**
     * Tests for isUsernameAvailable method
     * Rule: Username must be between 4 and 12 characters and not already exist in database
     */
    @Nested
    @DisplayName("isUsernameAvailable Tests")
    inner class IsUsernameAvailableTests {

        @Test
        @DisplayName("Should return true when username is available and has valid length")
        fun testIsUserNameAvailable_ValidLength_NotInDatabase() {
            // Arrange
            val username = "validuser"

            // Mock repository to return null (username not found)
            every { userRepository.findByUsername(username) } returns null

            // Act: Call the method
            val result = userService.isUsernameAvailable(username)

            // Assert: Verify the result
            assertTrue(result, "Username should be available when not in database and valid length")

            // Verify that the repository method was called with correct parameter
            verify(exactly = 1) { userRepository.findByUsername(username) }
        }

        @Test
        @DisplayName("Should return false when username already exists")
        fun testIsUsernameAvailable_AlreadyExists() {
            // Arrange
            val username = "existinguser"
            val existingUser = User(
                email = "test@example.com",
                username = username,
                photoUrl = null,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                firstName = "Test",
                lastName = "User"
            )

            // Mock repository to return an existing user
            every { userRepository.findByUsername(username) } returns existingUser

            // Act
            val result = userService.isUsernameAvailable(username)

            // Assert
            assertFalse(result, "Username should not be available when it already exists")
            verify(exactly = 1) { userRepository.findByUsername(username) }
        }

        @Test
        @DisplayName("Should return false when username is too short (less than 4 characters)")
        fun testIsUsernameAvailable_TooShort() {
            // Arrange
            val shortUsername = "abc" // 3 characters - below minimum

            // Act
            val result = userService.isUsernameAvailable(shortUsername)

            // Assert
            assertFalse(result, "Username with 3 characters should not be available (minimum is 4)")

            // Verify repository was never called (validation fails before database check)
            verify(exactly = 0) { userRepository.findByUsername(any()) }
        }

        @Test
        @DisplayName("Should return false when username is too long (more than 12 characters)")
        fun testIsUsernameAvailable_TooLong() {
            // Arrange
            val longUsername = "thisusernameistoolong" // 21 characters - above maximum

            // Act
            val result = userService.isUsernameAvailable(longUsername)

            // Assert
            assertFalse(result, "Username with 21 characters should not be available (maximum is 12)")
            verify(exactly = 0) { userRepository.findByUsername(any()) }
        }

        @Test
        @DisplayName("Should return true when username is exactly 4 characters (minimum boundary)")
        fun testIsUsernameAvailable_MinimumLength() {
            // Arrange
            val username = "user" // Exactly 4 characters
            every { userRepository.findByUsername(username) } returns null // Username not found

            // Act
            val result = userService.isUsernameAvailable(username)

            // Assert
            assertTrue(result, "Username with exactly 4 characters should be valid")
            verify(exactly = 1) { userRepository.findByUsername(username) }
        }

        @Test
        @DisplayName("Should return true when username is exactly 12 characters (maximum boundary)")
        fun testIsUsernameAvailable_MaximumLength() {
            // Arrange
            val username = "usernamelong" // Exactly 12 characters
            every { userRepository.findByUsername(username) } returns null // Username not found

            // Act
            val result = userService.isUsernameAvailable(username)

            // Assert
            assertTrue(result, "Username with exactly 12 characters should be valid")
            verify(exactly = 1) { userRepository.findByUsername(username) }
        }

        @Test
        @DisplayName("Should return false when username is 1 character")
        fun testIsUsernameAvailable_SingleCharacter() {
            // Arrange
            val username = "a"

            // Act
            val result = userService.isUsernameAvailable(username)

            // Assert
            assertFalse(result, "Single character username should not be valid")
            verify(exactly = 0) { userRepository.findByUsername(any()) } // No DB call
        }

        @Test
        @DisplayName("Should return false when username is empty string")
        fun testIsUsernameAvailable_EmptyString() {
            // Arrange
            val username = "" // Empty string

            // Act
            val result = userService.isUsernameAvailable(username)

            // Assert
            assertFalse(result, "Empty username should not be valid")
            verify(exactly = 0) { userRepository.findByUsername(any()) } // No DB call
        }
    }

    /**
     * Tests for isEmailAvailable method
     * Rule: Email must not already exist in database
     */
    @Nested
    @DisplayName("isEmailAvailable Tests")
    inner class IsEmailAvailableTests {

        @Test
        @DisplayName("Should return true when email is not in database")
        fun testIsEmailAvailable_NotInDatabase() {
            // Arrange
            val email = "newuser@example.com"

            // Mock repository to return null (email not found = available)
            every { userRepository.findByEmail(email) } returns null

            // Act
            val result = userService.isEmailAvailable(email)

            // Assert
            assertTrue(result, "Email should be available when it's not in the database")
            verify(exactly = 1) { userRepository.findByEmail(email) }
        }

        @Test
        @DisplayName("Should return false when email already exists in database")
        fun testIsEmailAvailable_AlreadyExists() {
            // Arrange
            val email = "existing@example.com"
            val existingUser = User(
                email = email,
                username = "existinguser",
                photoUrl = null,
                createdAt = "2024-01-01T12:00:00",
                updatedAt = "2024-01-01T12:00:00",
                firstName = "Existing",
                lastName = "User"
            )

            // Mock repository to return an existing user
            every { userRepository.findByEmail(email) } returns existingUser

            // Act
            val result = userService.isEmailAvailable(email)

            // Assert
            assertFalse(result, "Email should not be available when it already exists in database")
            verify(exactly = 1) { userRepository.findByEmail(email) }
        }

        @Test
        @DisplayName("Should handle email with different cases (repository handles case sensitivity)")
        fun testEmailAvailable_CaseVariation() {
            // Arrange
            val email = "Test.User@Example.COM"

            // Mock repository - in real implementation, Firestore query would handle case sensitivity
            every { userRepository.findByEmail(email) } returns null

            // Act
            val result = userService.isEmailAvailable(email)

            // Assert
            assertTrue(result, "Email availability check should work with various cases")
            verify(exactly = 1) { userRepository.findByEmail(email) }
        }
    }
}