package no.ntnu.prog2007.ihost.data.model.dto

import com.google.gson.annotations.SerializedName
import no.ntnu.prog2007.ihost.data.model.domain.Event
import no.ntnu.prog2007.ihost.data.model.domain.EventWithMetadata

/**
 * Nested Event object in API responses
 */
data class EventResponse(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("eventDate") val eventDate: String,
    @SerializedName("eventTime") val eventTime: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("creatorUid") val creatorUid: String,
    @SerializedName("free") val free: Boolean = true,
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("shareCode") val shareCode: String = ""
)

/**
 * EventWithMetadata API response
 */
data class EventWithMetadataResponse(
    @SerializedName("id") val id: String,
    @SerializedName("event") val event: EventResponse,
    @SerializedName("userStatus") val userStatus: String? = null,
    @SerializedName("userRole") val userRole: String? = null
)

/**
 * Request to create a new event
 */
data class CreateEventRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("eventDate")
    val eventDate: String,
    @SerializedName("eventTime")
    val eventTime: String? = null,
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("free")
    val free: Boolean = true,
    @SerializedName("price")
    val price: Double = 0.0
)

/**
 * Request to update an event
 */
data class UpdateEventRequest(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("eventDate")
    val eventDate: String? = null,
    @SerializedName("eventTime")
    val eventTime: String? = null,
    @SerializedName("location")
    val location: String? = null
)
