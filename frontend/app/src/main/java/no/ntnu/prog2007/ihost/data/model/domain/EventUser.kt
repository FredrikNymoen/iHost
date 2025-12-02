package no.ntnu.prog2007.ihost.data.model.domain

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
