package no.ntnu.prog2007.ihostapi.service.impl

import com.google.firebase.auth.FirebaseAuth
import no.ntnu.prog2007.ihostapi.exception.ResourceNotFoundException
import no.ntnu.prog2007.ihostapi.model.dto.CreateUserRequest
import no.ntnu.prog2007.ihostapi.model.dto.UpdateUserRequest
import no.ntnu.prog2007.ihostapi.model.dto.UserResponse
import no.ntnu.prog2007.ihostapi.model.entity.User
import no.ntnu.prog2007.ihostapi.repository.UserRepository
import no.ntnu.prog2007.ihostapi.service.UserService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

/**
 * Service implementation for User operations
 */
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : UserService {
    private val logger = Logger.getLogger(UserServiceImpl::class.java.name)

    override fun getUserById(uid: String): UserResponse? {
        val user = userRepository.findById(uid) ?: return null
        return mapToUserResponse(user, uid)
    }

    override fun createUser(request: CreateUserRequest): User {
        // Verify user exists in Firebase Auth
        val userRecord = try {
            firebaseAuth.getUser(request.uid)
        } catch (e: Exception) {
            logger.warning("User not found in Firebase Auth: ${request.uid}")
            throw ResourceNotFoundException("User not found in Firebase Auth")
        }

        // Check if profile already exists
        val existingUser = userRepository.findById(request.uid)
        if (existingUser != null) {
            logger.warning("User profile already exists for UID: ${request.uid}")
            throw IllegalArgumentException("User profile already exists")
        }

        // Create User model instance
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val timestamp = now.format(formatter)

        val user = User(
            email = request.email,
            username = request.username,
            photoUrl = request.photoUrl,
            createdAt = timestamp,
            updatedAt = timestamp,
            firstName = request.firstName,
            lastName = request.lastName
        )

        return userRepository.save(user, request.uid)
    }

    override fun updateUser(uid: String, request: UpdateUserRequest): UserResponse {
        val currentUser = userRepository.findById(uid)
            ?: throw ResourceNotFoundException("User not found")

        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val timestamp = now.format(formatter)

        // Update only non-null fields
        val updatedUser = currentUser.copy(
            firstName = request.firstName ?: currentUser.firstName,
            lastName = request.lastName ?: currentUser.lastName,
            photoUrl = request.photoUrl ?: currentUser.photoUrl,
            updatedAt = timestamp
        )

        val savedUser = userRepository.save(updatedUser, uid)
        return mapToUserResponse(savedUser, uid)
    }

    override fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { (uid, user) ->
            mapToUserResponse(user, uid)
        }
    }

    /**
     * Helper function to map User entity to UserResponse DTO
     */
    private fun mapToUserResponse(user: User, uid: String): UserResponse {
        return UserResponse(
            uid = uid,
            email = user.email,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            photoUrl = user.photoUrl,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }

    override fun isUsernameAvailable(username: String): Boolean {
        if (username.length !in 4..12) {
            return false
        }
        return userRepository.findByUsername(username) == null
    }

    override fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    override fun isEmailAvailable(email: String): Boolean {
        return userRepository.findByEmail(email) == null
    }
}
