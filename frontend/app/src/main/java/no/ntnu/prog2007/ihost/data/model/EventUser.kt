package no.ntnu.prog2007.ihost.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a user's relationship to an event
 */
data class EventUser(
    @SerializedName("id")
    val id: String,

    @SerializedName("eventId")
    val eventId: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("status")
    val status: String, // PENDING, ACCEPTED, DECLINED, CREATOR

    @SerializedName("role")
    val role: String, // CREATOR, ATTENDEE

    @SerializedName("invitedAt")
    val invitedAt: String,

    @SerializedName("respondedAt")
    val respondedAt: String?
)

/**
 * Response containing event with metadata
 */
data class EventWithMetadata(
    @SerializedName("id")
    val id: String,

    @SerializedName("event")
    val event: Event,

    @SerializedName("userStatus")
    val userStatus: String? = null,

    @SerializedName("userRole")
    val userRole: String? = null
)

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
