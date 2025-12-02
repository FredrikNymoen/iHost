package no.ntnu.prog2007.ihost.data.model.dto

import com.google.gson.annotations.SerializedName
import no.ntnu.prog2007.ihost.data.model.domain.Event

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
