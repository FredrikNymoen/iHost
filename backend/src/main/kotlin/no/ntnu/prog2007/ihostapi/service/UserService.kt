package no.ntnu.prog2007.ihostapi.service

import no.ntnu.prog2007.ihostapi.model.dto.CreateUserRequest
import no.ntnu.prog2007.ihostapi.model.dto.UpdateUserRequest
import no.ntnu.prog2007.ihostapi.model.dto.UserResponse
import no.ntnu.prog2007.ihostapi.model.entity.User

/**
 * Service interface for User operations
 */
interface UserService {
    fun getUserById(uid: String): UserResponse?
    fun createUser(request: CreateUserRequest): User
    fun updateUser(uid: String, request: UpdateUserRequest): UserResponse
    fun getAllUsers(): List<UserResponse>
    fun isUsernameAvailable(username: String): Boolean
    fun getUserByUsername(username: String): User?
    fun isEmailAvailable(email: String): Boolean
}
