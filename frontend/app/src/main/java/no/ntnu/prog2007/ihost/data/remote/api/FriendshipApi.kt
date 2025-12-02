package no.ntnu.prog2007.ihost.data.remote.api

import no.ntnu.prog2007.ihost.data.model.domain.Friendship
import no.ntnu.prog2007.ihost.data.model.dto.FriendRequestRequest
import no.ntnu.prog2007.ihost.data.remote.config.ApiEndpoints.FRIENDSHIPS
import retrofit2.http.*

interface FriendshipApi {
    @POST("$FRIENDSHIPS/request")
    suspend fun sendFriendRequest(
        @Body request: FriendRequestRequest
    ): Friendship

    @POST("$FRIENDSHIPS/{friendshipId}/accept")
    suspend fun acceptFriendRequest(
        @Path("friendshipId") friendshipId: String
    ): Friendship

    @POST("$FRIENDSHIPS/{friendshipId}/decline")
    suspend fun declineFriendRequest(
        @Path("friendshipId") friendshipId: String
    ): Friendship

    @DELETE("$FRIENDSHIPS/{friendshipId}")
    suspend fun removeFriend(
        @Path("friendshipId") friendshipId: String
    ): Map<String, Any>

    @GET("$FRIENDSHIPS/pending")
    suspend fun getPendingRequests(): List<Friendship>

    @GET("$FRIENDSHIPS/friends")
    suspend fun getFriends(): List<Friendship>

    @GET("$FRIENDSHIPS/sent")
    suspend fun getSentRequests(): List<Friendship>
}
