package no.ntnu.prog2007.ihost.data.remote.api

import no.ntnu.prog2007.ihost.data.model.domain.Friendship
import no.ntnu.prog2007.ihost.data.model.dto.FriendRequestRequest
import no.ntnu.prog2007.ihost.data.remote.config.ApiEndpoints.FRIENDSHIPS
import retrofit2.http.*

/**
 * Retrofit API interface for friendship management
 *
 * Handles friend requests and friendships between users.
 * Friendships have statuses: PENDING (request sent), ACCEPTED (friends).
 */
interface FriendshipApi {
    /**
     * Send a friend request to another user
     *
     * Creates a new friendship with PENDING status.
     *
     * @param request Friend request with recipient user ID
     * @return Created friendship record
     */
    @POST("$FRIENDSHIPS/request")
    suspend fun sendFriendRequest(
        @Body request: FriendRequestRequest
    ): Friendship

    /**
     * Accept a friend request
     *
     * Updates friendship status from PENDING to ACCEPTED.
     *
     * @param friendshipId The friendship document ID
     * @return Updated friendship record
     */
    @POST("$FRIENDSHIPS/{friendshipId}/accept")
    suspend fun acceptFriendRequest(
        @Path("friendshipId") friendshipId: String
    ): Friendship

    /**
     * Decline a friend request
     *
     * Deletes the friendship record.
     *
     * @param friendshipId The friendship document ID
     * @return Deleted friendship record
     */
    @POST("$FRIENDSHIPS/{friendshipId}/decline")
    suspend fun declineFriendRequest(
        @Path("friendshipId") friendshipId: String
    ): Friendship

    /**
     * Remove a friend (unfriend)
     *
     * Deletes an accepted friendship, removing the friend relationship.
     *
     * @param friendshipId The friendship document ID
     * @return Success response map
     */
    @DELETE("$FRIENDSHIPS/{friendshipId}")
    suspend fun removeFriend(
        @Path("friendshipId") friendshipId: String
    ): Map<String, Any>

    /**
     * Get incoming friend requests for the current user
     *
     * Returns friendships with PENDING status where current user is recipient.
     *
     * @return List of pending friend requests
     */
    @GET("$FRIENDSHIPS/pending")
    suspend fun getPendingRequests(): List<Friendship>

    /**
     * Get all friends for the current user
     *
     * Returns friendships with ACCEPTED status.
     *
     * @return List of accepted friendships
     */
    @GET("$FRIENDSHIPS/friends")
    suspend fun getFriends(): List<Friendship>

    /**
     * Get outgoing friend requests for the current user
     *
     * Returns friendships with PENDING status where current user is sender.
     *
     * @return List of sent friend requests
     */
    @GET("$FRIENDSHIPS/sent")
    suspend fun getSentRequests(): List<Friendship>
}
