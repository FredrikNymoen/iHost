package no.ntnu.prog2007.ihostapi.config

import com.stripe.Stripe
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import jakarta.annotation.PostConstruct
import java.util.logging.Logger

@Configuration
class StripeConfig {

    @Value("\${STRIPE_SECRET_KEY}")
    private lateinit var stripeSecretKey: String

    @Value("\${STRIPE_PUBLISHABLE_KEY}")
    lateinit var stripePublishableKey: String

    @Value("\${STRIPE_ENDPOINT_SECRET}")
    lateinit var stripeEndpointSecret: String

    private val logger = Logger.getLogger(StripeConfig::class.java.name)

    @PostConstruct
    fun initStripe() {
        Stripe.apiKey = stripeSecretKey
        logger.info("Stripe API initialized with key: ${stripeSecretKey.take(7)}...")
    }
}