package no.ntnu.prog2007.ihost.data.model.domain

import com.google.gson.annotations.SerializedName

/**
 * Event domain model
 * Note: ID is stored separately in EventWithMetadata wrapper
 * Note: Attendees are managed through event_users collection
 */
data class Event(
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
    @SerializedName("creatorUid")
    val creatorUid: String,
    @SerializedName("free")
    val free: Boolean = true,
    @SerializedName("price")
    val price: Double = 0.0,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("shareCode")
    val shareCode: String = ""
)
