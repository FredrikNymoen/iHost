package no.ntnu.prog2007.ihost.data.model.dto

import com.google.gson.annotations.SerializedName
import no.ntnu.prog2007.ihost.data.model.domain.EventUser

/**
 * Request to invite users to an event
 */
data class InviteUsersRequest(
    @SerializedName("eventId")
    val eventId: String,
    @SerializedName("userIds")
    val userIds: List<String>
)

/**
 * Response from invite users endpoint
 */
data class InviteUsersResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("invitedCount")
    val invitedCount: Int,
    @SerializedName("invitedUsers")
    val invitedUsers: List<EventUser>
)
