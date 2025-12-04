package no.ntnu.prog2007.ihostapi.service.impl

import com.google.cloud.firestore.Firestore
import com.google.firebase.auth.FirebaseAuth
import no.ntnu.prog2007.ihostapi.exception.ResourceNotFoundException
import no.ntnu.prog2007.ihostapi.model.dto.CreateUserRequest
import no.ntnu.prog2007.ihostapi.model.dto.UpdateUserRequest
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
    private val firebaseAuth: FirebaseAuth,
    private val firestore: Firestore
) : UserService {
    private val logger = Logger.getLogger(UserServiceImpl::class.java.name)

    override fun getUserById(uid: String): User? {
        return userRepository.findById(uid)
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
            uid = request.uid,
            email = request.email,
            username = request.username,
            phoneNumber = request.phoneNumber,
            photoUrl = request.photoUrl,
            createdAt = timestamp,
            updatedAt = timestamp,
            isEmailVerified = userRecord.isEmailVerified,
            firstName = request.firstName,
            lastName = request.lastName
        )

        return userRepository.save(user)
    }

    override fun updateUser(uid: String, request: UpdateUserRequest): User {
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
            phoneNumber = request.phoneNumber ?: currentUser.phoneNumber,
            updatedAt = timestamp
        )

        return userRepository.save(updatedUser)
    }

    override fun getAllUsers(): List<User> {
        val usersQuery = firestore.collection(UserRepository.COLLECTION_NAME)
            .get()
            .get()

        return usersQuery.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)
        }
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
