package no.ntnu.prog2007.ihost.data.repository

import android.util.Log
import no.ntnu.prog2007.ihost.data.model.dto.KeysResponse
import no.ntnu.prog2007.ihost.data.model.dto.PaymentIntentRequest
import no.ntnu.prog2007.ihost.data.model.dto.PaymentIntentResponse
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
     * Get full keys response (for advanced use cases)
     */
    suspend fun getKeys(): Result<KeysResponse> {
        return try {
            val keys = stripeApi.getKeys()
            Log.d("StripeRepository", "Retrieved Stripe keys")
            Result.success(keys)
        } catch (e: Exception) {
            Log.e("StripeRepository", "Error retrieving Stripe keys", e)
            Result.failure(e)
        }
    }

    /**
     * Create a payment intent for an event
     * @param eventId The ID of the event to create payment for
     */
    suspend fun createPaymentIntent(eventId: String): Result<PaymentIntentResponse> {
        return try {
            val request = PaymentIntentRequest(eventId = eventId)
            val response = stripeApi.createPaymentIntent(request)
            Log.d("StripeRepository", "Created payment intent for event: $eventId")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("StripeRepository", "Error creating payment intent for event: $eventId", e)
            Result.failure(e)
        }
    }
}
