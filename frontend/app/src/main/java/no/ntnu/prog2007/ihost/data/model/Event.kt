package no.ntnu.prog2007.ihost.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class Event(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("eventDate")
    val eventDate: String, // ISO format

    @SerializedName("eventTime")
    val eventTime: String? = null, // HH:mm format

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("creatorUid")
    val creatorUid: String,

    @SerializedName("creatorName")
    val creatorName: String? = null,

    @SerializedName("attendees")
    val attendees: List<String> = emptyList(),

    @SerializedName("createdAt")
    val createdAt: LocalDateTime? = null,

    @SerializedName("updatedAt")
    val updatedAt: LocalDateTime? = null
)

data class CreateEventRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("eventDate")
    val eventDate: String, // ISO format

    @SerializedName("eventTime")
    val eventTime: String? = null, // HH:mm format

    @SerializedName("location")
    val location: String? = null
)
