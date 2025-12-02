package no.ntnu.prog2007.ihost.data.remote.api

import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.data.model.dto.AuthResponse
import no.ntnu.prog2007.ihost.data.model.dto.CreateUserRequest
import no.ntnu.prog2007.ihost.data.model.dto.UpdateUserRequest
import no.ntnu.prog2007.ihost.data.remote.config.ApiEndpoints.USERS
import retrofit2.http.*

interface UserApi {
    @POST("$USERS/register")
    suspend fun registerUser(
        @Body request: CreateUserRequest
    ): AuthResponse

    @GET(USERS)
    suspend fun getAllUsers(): List<User>

    @GET("$USERS/{uid}")
    suspend fun getUserByUid(
        @Path("uid") uid: String
    ): User

    @GET("$USERS/username-available/{username}")
    suspend fun isUsernameAvailable(
        @Path("username") username: String
    ): Map<String, Boolean>

    @PUT("$USERS/{uid}")
    suspend fun updateUserProfile(
        @Path("uid") uid: String,
        @Body request: UpdateUserRequest
    ): User
}
