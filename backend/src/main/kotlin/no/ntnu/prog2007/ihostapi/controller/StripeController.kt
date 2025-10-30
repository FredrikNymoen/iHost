package no.ntnu.prog2007.ihostapi.controller

import no.ntnu.prog2007.ihostapi.config.StripeConfig
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import com.stripe.model.PaymentIntent
import com.stripe.model.Customer
import com.stripe.model.EphemeralKey
import com.stripe.param.CustomerCreateParams
import com.stripe.param.EphemeralKeyCreateParams
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.exception.SignatureVerificationException
import com.stripe.net.Webhook
import java.util.logging.Logger
import java.util.logging.Level

data class PaymentIntentRequest(
    val orderId: String
)

data class PaymentIntentResponse(
    val paymentIntent: String,
    val ephemeralKey: String,
    val customer: String,
    val publishableKey: String?
)

data class WebhookResponse(
    val received: Boolean
)

@RestController
@RequestMapping("/api/stripe")
class StripeController(
    private val stripeConfig: StripeConfig
) {

    private val logger = Logger.getLogger(StripeController::class.java.name)

    @PostMapping("/payment-intent")
    fun createPaymentIntent(@RequestBody request: PaymentIntentRequest): ResponseEntity<Any> {
        return try {
            val orderId = request.orderId
            logger.info("Creating payment intent for order: $orderId")

            // TODO: Fetch order items from database
            // val orderItems = db.select().from(orderItemsTable)
            //     .where(eq(orderItemsTable.orderId, orderId))

            // TODO: Calculate total sum of order items (price * quantity)
            // val total = orderItems.sumOf { it.price * it.quantity }

            // Placeholder amount for testing (10.00 USD)
            val total = 10.00
            val amount = (total * 100).toLong()

            if (amount <= 0L) {
                logger.warning("Invalid order amount: $amount")
                return ResponseEntity.badRequest()
                    .body(mapOf("message" to "Order total must be greater than 0"))
            }

            // Create customer
            val customerParams = CustomerCreateParams.builder()
                // TODO: Add customer email and other info when available
                // .setEmail(userEmail)
                // .setName(userName)
                .putMetadata("orderId", orderId)
                .build()
            val customer = Customer.create(customerParams)
            logger.info("Created customer: ${customer.id}")

            // Create ephemeral key for the customer
            val ephemeralKeyParams = EphemeralKeyCreateParams.builder()
                .setCustomer(customer.id)
                // Use the correct API version string
                .setStripeVersion("2024-10-28.acacia")
                .build()
            val ephemeralKey = EphemeralKey.create(ephemeralKeyParams)
            logger.info("Created ephemeral key for customer: ${customer.id}")

            // Create payment intent
            val paymentIntentParams = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency("usd")
                .setCustomer(customer.id)
                .putMetadata("orderId", orderId)
                // Optional: Add automatic payment methods
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build()
            val paymentIntent = PaymentIntent.create(paymentIntentParams)
            logger.info("Created payment intent: ${paymentIntent.id} for amount: $amount")

            // TODO: Store paymentIntent.id in order database
            // db.update(ordersTable)
            //     .set(ordersTable.stripePaymentIntentId to paymentIntent.id)
            //     .where(eq(ordersTable.id, orderId))

            val response = PaymentIntentResponse(
                paymentIntent = paymentIntent.clientSecret ?: "",
                ephemeralKey = ephemeralKey.secret ?: "",
                customer = customer.id,
                publishableKey = stripeConfig.stripePublishableKey
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error creating payment intent", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to (e.message ?: "Unknown error")))
        }
    }

    @PostMapping("/webhook")
    fun webhook(
        @RequestBody body: String,
        @RequestHeader("stripe-signature") sig: String
    ): ResponseEntity<WebhookResponse> {
        return try {
            logger.info("Received webhook with signature: ${sig.take(20)}...")

            val event = try {
                Webhook.constructEvent(body, sig, stripeConfig.stripeEndpointSecret)
            } catch (e: SignatureVerificationException) {
                logger.log(Level.SEVERE, "Invalid signature", e)
                return ResponseEntity.badRequest()
                    .body(WebhookResponse(received = false))
            }

            logger.info("Processing webhook event: ${event.type}")

            // Handle the event
            when (event.type) {
                "payment_intent.succeeded" -> {
                    val paymentIntent = event.dataObjectDeserializer.getObject().orElse(null) as? PaymentIntent
                    paymentIntent?.let {
                        logger.info("Payment succeeded for PaymentIntent: ${it.id}")
                        val orderId = it.metadata["orderId"]
                        // TODO: Update order status to "paid"
                        // db.update(ordersTable)
                        //     .set(ordersTable.status to "paid")
                        //     .where(eq(ordersTable.stripePaymentIntentId, it.id))
                    }
                }
                "payment_intent.payment_failed" -> {
                    val paymentIntent = event.dataObjectDeserializer.getObject().orElse(null) as? PaymentIntent
                    paymentIntent?.let {
                        logger.warning("Payment failed for PaymentIntent: ${it.id}")
                        val orderId = it.metadata["orderId"]
                        // TODO: Update order status to "payment_failed"
                        // db.update(ordersTable)
                        //     .set(ordersTable.status to "payment_failed")
                        //     .where(eq(ordersTable.stripePaymentIntentId, it.id))
                    }
                }
                "payment_method.attached" -> {
                    logger.info("Payment method attached")
                    // TODO: Handle payment method attachment if needed
                }
                else -> {
                    logger.info("Unhandled event type: ${event.type}")
                }
            }

            ResponseEntity.ok(WebhookResponse(received = true))
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error processing webhook", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(WebhookResponse(received = false))
        }
    }
}