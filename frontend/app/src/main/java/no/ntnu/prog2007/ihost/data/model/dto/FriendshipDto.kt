package no.ntnu.prog2007.ihost.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Request to send a friend request
 */
data class FriendRequestRequest(
    @SerializedName("toUserId")
    val toUserId: String
)
