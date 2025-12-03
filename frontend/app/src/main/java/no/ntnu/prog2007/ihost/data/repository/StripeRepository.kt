package no.ntnu.prog2007.ihost.data.repository

import android.util.Log
import no.ntnu.prog2007.ihost.data.model.domain.PaymentIntent
import no.ntnu.prog2007.ihost.data.model.dto.PaymentIntentRequest
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient
import no.ntnu.prog2007.ihost.data.remote.api.StripeApi

/**
 * Repository for Stripe payment operations
 * Handles payment intent creation and key retrieval
 */
class StripeRepository(
    private val stripeApi: StripeApi = RetrofitClient.stripeApi
) {

    /**
     * Get Stripe publishable key
     */
    suspend fun getPublishableKey(): Result<String> {
        return try {
            val response = stripeApi.getKeys()
            Log.d("StripeRepository", "Retrieved Stripe publishable key")
            Result.success(response.publishableKey)
        } catch (e: Exception) {
            Log.e("StripeRepository", "Error retrieving Stripe keys", e)
            Result.failure(e)
        }
    }


    /**
     * Create a payment intent for an event
     * @param eventId The ID of the event to create payment for
     * @return Result containing PaymentIntent domain model
     */
    suspend fun createPaymentIntent(eventId: String): Result<PaymentIntent> {
        return try {
            val request = PaymentIntentRequest(eventId = eventId)
            val response = stripeApi.createPaymentIntent(request)
            Log.d("StripeRepository", "Created payment intent for event: $eventId")

            // Map DTO to domain model
            val paymentIntent = PaymentIntent(
                clientSecret = response.paymentIntent,
                ephemeralKey = response.ephemeralKey,
                customerId = response.customer,
                publishableKey = response.publishableKey
            )
            Result.success(paymentIntent)
        } catch (e: Exception) {
            Log.e("StripeRepository", "Error creating payment intent for event: $eventId", e)
            Result.failure(e)
        }
    }
}
