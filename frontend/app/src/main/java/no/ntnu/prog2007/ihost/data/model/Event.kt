package no.ntnu.prog2007.ihost.data.model

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("eventDate")
    val eventDate: String, // ISO format YYYY-MM-DD

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

    @SerializedName("free")
    val free: Boolean = true,

    @SerializedName("price")
    val price: Double = 0.0,

    @SerializedName("createdAt")
    val createdAt: String? = null, // ISO 8601 format

    @SerializedName("updatedAt")
    val updatedAt: String? = null // ISO 8601 format
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
    val location: String? = null,

    @SerializedName("free")
    val free: Boolean = true,

    @SerializedName("price")
    val price: Double = 0.0
)

data class PaymentIntentRequest(
    @SerializedName("eventId")
    val eventId: String
)

data class PaymentIntentResponse(
    @SerializedName("paymentIntent")
    val paymentIntent: String,

    @SerializedName("ephemeralKey")
    val ephemeralKey: String,

    @SerializedName("customer")
    val customer: String,

    @SerializedName("publishableKey")
    val publishableKey: String?
)
