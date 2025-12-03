package no.ntnu.prog2007.ihost.data.model.domain

/**
 * Domain model for payment intent data
 * Contains the necessary information to present a Stripe payment sheet
 */
data class PaymentIntent(
    val clientSecret: String,
    val ephemeralKey: String,
    val customerId: String,
    val publishableKey: String?
)
