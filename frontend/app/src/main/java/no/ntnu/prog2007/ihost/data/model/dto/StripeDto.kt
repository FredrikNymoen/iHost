package no.ntnu.prog2007.ihost.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Request to create a payment intent
 */
data class PaymentIntentRequest(
    @SerializedName("eventId")
    val eventId: String
)

/**
 * Response from payment intent creation
 */
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

/**
 * Response containing Stripe publishable key
 */
data class KeysResponse(
    @SerializedName("publishableKey")
    val publishableKey: String
)
