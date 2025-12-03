package no.ntnu.prog2007.ihost.service

import androidx.activity.ComponentActivity
import android.util.Log
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.ntnu.prog2007.ihost.data.repository.StripeRepository

/**
 * Service for handling Stripe payment operations
 * Uses StripeRepository for API communication
 */
class StripePaymentService(
    private val stripeRepository: StripeRepository,
    private val activity: ComponentActivity
) {
    companion object {
        private const val TAG = "StripePaymentService"
        private var paymentSheet: PaymentSheet? = null
        private var onPaymentComplete: (() -> Unit)? = null
        private var onPaymentFailed: ((String) -> Unit)? = null

        fun initializePaymentSheet(activity: ComponentActivity) {
            if (paymentSheet == null) {
                Log.d(TAG, "Initializing PaymentSheet in activity")
                paymentSheet = PaymentSheet(activity) { result ->
                    handlePaymentSheetResult(result)
                }
            }
        }

        fun setCallbacks(
            onComplete: (() -> Unit)? = null,
            onFailed: ((String) -> Unit)? = null
        ) {
            onPaymentComplete = onComplete
            onPaymentFailed = onFailed
        }

        private fun handlePaymentSheetResult(result: PaymentSheetResult) {
            when (result) {
                is PaymentSheetResult.Completed -> {
                    Log.d(TAG, "Payment completed successfully")
                    onPaymentComplete?.invoke()
                }
                is PaymentSheetResult.Canceled -> {
                    Log.d(TAG, "Payment canceled by user")
                    onPaymentFailed?.invoke("Payment was canceled")
                }
                is PaymentSheetResult.Failed -> {
                    Log.e(TAG, "Payment failed: ${result.error.message}")
                    onPaymentFailed?.invoke(result.error.message ?: "Payment failed")
                }
            }
            clearCallbacks()
        }

        private fun clearCallbacks() {
            onPaymentComplete = null
            onPaymentFailed = null
        }

        fun getPaymentSheet(): PaymentSheet? = paymentSheet
    }

    suspend fun initiatePayment(
        eventId: String,
        onComplete: () -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) = withContext(Dispatchers.Main) {
        try {
            Log.d(TAG, "Starting payment initiation for event: $eventId")

            // Set callbacks before initiating payment
            setCallbacks(onComplete, onFailed)

            // 1) Hent publishable key
            val publishableKeyResult = stripeRepository.getPublishableKey()
            val publishableKey = publishableKeyResult.getOrElse {
                throw IllegalStateException("Failed to get publishable key: ${it.message}")
            }
            PaymentConfiguration.init(activity.applicationContext, publishableKey)
            Log.d(TAG, "PaymentConfiguration initialized")

            // 2) Lag PaymentIntent (backend)
            val paymentIntentResult = stripeRepository.createPaymentIntent(eventId)
            val paymentIntent = paymentIntentResult.getOrElse {
                throw IllegalStateException("Failed to create payment intent: ${it.message}")
            }
            val clientSecret = paymentIntent.clientSecret
            if (clientSecret.isNullOrEmpty()) {
                throw IllegalStateException("Missing client secret from backend")
            }
            Log.d(TAG, "Payment intent received from backend")

            // 3) Get the pre-initialized PaymentSheet
            val sheet = getPaymentSheet()
                ?: throw IllegalStateException("PaymentSheet not initialized. Call initializePaymentSheet() in MainActivity.onCreate()")

            // 4) Konfigurer kunde
            val customerConfig = PaymentSheet.CustomerConfiguration(
                id = paymentIntent.customerId,
                ephemeralKeySecret = paymentIntent.ephemeralKey
            )
            val configuration = PaymentSheet.Configuration(
                merchantDisplayName = "iHost",
                customer = customerConfig,
                allowsDelayedPaymentMethods = false,
                defaultBillingDetails = PaymentSheet.BillingDetails(
                    address = PaymentSheet.Address(
                        country = "NO" // Set default country to Norway
                    )
                )
            )

            // 5) Present payment sheet
            sheet.presentWithPaymentIntent(
                clientSecret,
                configuration
            )
            Log.d(TAG, "Payment sheet presented to user")

        } catch (e: Exception) {
            Log.e(TAG, "Error initiating payment", e)
            onPaymentFailed?.invoke(e.message ?: "Payment initialization failed")
            clearCallbacks()
        }
    }
}
