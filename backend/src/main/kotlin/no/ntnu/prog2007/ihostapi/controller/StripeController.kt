package no.ntnu.prog2007.ihostapi.controller

import no.ntnu.prog2007.ihostapi.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

@RestController
@RequestMapping("/api/stripe")
class StripeController(
    private val paymentService: PaymentService
) {
    private val logger = Logger.getLogger(StripeController::class.java.name)

    /**
     * Create a payment intent for an event
     */
    @PostMapping("/payment-intent")
    fun createPaymentIntent(@RequestBody request: PaymentIntentRequest): ResponseEntity<PaymentIntentResponse> {
        val result = paymentService.createPaymentIntent(request.eventId)

        return ResponseEntity.ok(PaymentIntentResponse(
            paymentIntent = result.paymentIntent,
            ephemeralKey = result.ephemeralKey,
            customer = result.customer,
            publishableKey = result.publishableKey
        ))
    }

    /**
     * Get Stripe publishable key
     */
    @GetMapping("/keys")
    fun getKeys(): ResponseEntity<KeysResponse> {
        val publishableKey = paymentService.getPublishableKey()
        return ResponseEntity.ok(KeysResponse(publishableKey = publishableKey))
    }

    /**
     * Handle Stripe webhook events
     */
    @PostMapping("/webhook")
    fun webhook(
        @RequestBody body: String,
        @RequestHeader("stripe-signature") signature: String
    ): ResponseEntity<WebhookResponse> {
        val success = paymentService.handleWebhook(body, signature)

        return if (success) {
            ResponseEntity.ok(WebhookResponse(received = true))
        } else {
            ResponseEntity.badRequest()
                .body(WebhookResponse(received = false))
        }
    }
}

/**
 * Request to create a payment intent
 */
data class PaymentIntentRequest(
    val eventId: String
)

/**
 * Response with payment intent details
 */
data class PaymentIntentResponse(
    val paymentIntent: String,
    val ephemeralKey: String,
    val customer: String,
    val publishableKey: String?
)

/**
 * Response for webhook processing
 */
data class WebhookResponse(
    val received: Boolean
)

/**
 * Response with Stripe publishable key
 */
data class KeysResponse(
    val publishableKey: String?
)
