package no.ntnu.prog2007.ihost.data.remote.api

import no.ntnu.prog2007.ihost.data.model.dto.KeysResponse
import no.ntnu.prog2007.ihost.data.model.dto.PaymentIntentRequest
import no.ntnu.prog2007.ihost.data.model.dto.PaymentIntentResponse
import no.ntnu.prog2007.ihost.data.remote.config.ApiEndpoints.STRIPE
import retrofit2.http.*

interface StripeApi {
    @GET("$STRIPE/keys")
    suspend fun getKeys(): KeysResponse

    @POST("$STRIPE/payment-intent")
    suspend fun createPaymentIntent(
        @Body request: PaymentIntentRequest
    ): PaymentIntentResponse
}
