package no.ntnu.prog2007.ihostapi.service.impl

import com.stripe.exception.SignatureVerificationException
import com.stripe.model.Customer
import com.stripe.model.EphemeralKey
import com.stripe.model.PaymentIntent
import com.stripe.net.Webhook
import com.stripe.param.CustomerCreateParams
import com.stripe.param.EphemeralKeyCreateParams
import com.stripe.param.PaymentIntentCreateParams
import no.ntnu.prog2007.ihostapi.config.StripeConfig
import no.ntnu.prog2007.ihostapi.exception.ResourceNotFoundException
import no.ntnu.prog2007.ihostapi.repository.EventRepository
import no.ntnu.prog2007.ihostapi.service.PaymentIntentResult
import no.ntnu.prog2007.ihostapi.service.PaymentService
import org.springframework.stereotype.Service
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Service implementation for payment operations using Stripe
 */
@Service
class PaymentServiceImpl(
    private val stripeConfig: StripeConfig,
    private val eventRepository: EventRepository
) : PaymentService {
    private val logger = Logger.getLogger(PaymentServiceImpl::class.java.name)

    override fun createPaymentIntent(eventId: String): PaymentIntentResult {
        logger.info("Creating payment intent for event: $eventId")

        // Fetch event from database
        val event = eventRepository.findById(eventId)
            ?: throw ResourceNotFoundException("Event not found")

        val total = event.price

        // Convert to cents (Stripe uses smallest currency unit)
        val amount = (total * 100).toLong()

        if (amount <= 0) {
            logger.warning("Invalid order amount: $amount")
            throw IllegalArgumentException("Order total must be greater than 0")
        }

        // Create customer
        val customerParams = CustomerCreateParams.builder()
            // TODO: Add customer email and other info when available
            // .setEmail(userEmail)
            // .setName(userName)
            .build()
        val customer = Customer.create(customerParams)
        logger.info("Created customer: ${customer.id}")

        // Create ephemeral key for the customer
        val ephemeralKeyParams = EphemeralKeyCreateParams.builder()
            .setCustomer(customer.id)
            .setStripeVersion("2024-10-28.acacia")
            .build()
        val ephemeralKey = EphemeralKey.create(ephemeralKeyParams)
        logger.info("Created ephemeral key for customer: ${customer.id}")

        // Create payment intent
        val paymentIntentParams = PaymentIntentCreateParams.builder()
            .setAmount(amount)
            .setCurrency("nok")
            .setCustomer(customer.id)
            .putMetadata("eventId", eventId)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .build()
        val paymentIntent = PaymentIntent.create(paymentIntentParams)
        logger.info("Created payment intent: ${paymentIntent.id} for amount: $amount")

        // TODO: Store paymentIntent.id in order database if needed

        return PaymentIntentResult(
            paymentIntent = paymentIntent.clientSecret ?: "",
            ephemeralKey = ephemeralKey.secret ?: "",
            customer = customer.id,
            publishableKey = stripeConfig.stripePublishableKey
        )
    }

    override fun getPublishableKey(): String? {
        logger.info("Fetching Stripe publishable key")
        return stripeConfig.stripePublishableKey
    }

    override fun handleWebhook(body: String, signature: String): Boolean {
        return try {
            logger.info("Received webhook with signature: ${signature.take(20)}...")

            val event = try {
                Webhook.constructEvent(body, signature, stripeConfig.stripeEndpointSecret)
            } catch (e: SignatureVerificationException) {
                logger.log(Level.SEVERE, "Invalid signature", e)
                return false
            }

            logger.info("Processing webhook event: ${event.type}")

            // Handle the event
            when (event.type) {
                "payment_intent.succeeded" -> {
                    val paymentIntent = event.dataObjectDeserializer.getObject().orElse(null) as? PaymentIntent
                    paymentIntent?.let {
                        logger.info("Payment succeeded for PaymentIntent: ${it.id}")
                        // TODO: Update order status to "paid"
                        // val eventId = it.metadata["eventId"]
                    }
                }
                "payment_intent.payment_failed" -> {
                    val paymentIntent = event.dataObjectDeserializer.getObject().orElse(null) as? PaymentIntent
                    paymentIntent?.let {
                        logger.warning("Payment failed for PaymentIntent: ${it.id}")
                        // TODO: Update order status to "payment_failed"
                        // val eventId = it.metadata["eventId"]
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

            true
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error processing webhook", e)
            false
        }
    }
}
