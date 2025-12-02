package no.ntnu.prog2007.ihostapi.service

/**
 * Service interface for payment operations
 */
interface PaymentService {
    fun createPaymentIntent(eventId: String): PaymentIntentResult
    fun getPublishableKey(): String?
    fun handleWebhook(body: String, signature: String): Boolean
}

/**
 * Result of creating a payment intent
 */
data class PaymentIntentResult(
    val paymentIntent: String,
    val ephemeralKey: String,
    val customer: String,
    val publishableKey: String?
)
