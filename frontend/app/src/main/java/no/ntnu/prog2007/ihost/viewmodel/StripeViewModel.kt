package no.ntnu.prog2007.ihost.viewmodel

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.ntnu.prog2007.ihost.data.model.PaymentIntentRequest
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient
import no.ntnu.prog2007.ihost.service.StripePaymentService
import android.util.Log

data class StripeUiState(
    val publishableKey: String? = null,
    val isProcessingPayment: Boolean = false,
    val paymentError: String? = null,
    val paymentSuccess: Boolean = false
)

class StripeViewModel : ViewModel() {
    companion object {
        private const val TAG = "StripeViewModel"
    }

    private val _uiState = MutableStateFlow(StripeUiState())
    val uiState: StateFlow<StripeUiState> = _uiState

    init {
        loadPublishableKey()
    }

    private fun loadPublishableKey() {
        viewModelScope.launch {
            try {
                val key = RetrofitClient.apiService.getKeys().publishableKey
                _uiState.value = _uiState.value.copy(publishableKey = key)
                Log.d(TAG, "Publishable key loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading publishable key", e)
                _uiState.value = _uiState.value.copy(
                    paymentError = "Failed to load payment configuration: ${e.message}"
                )
            }
        }
    }

    fun initiatePayment(
        activity: ComponentActivity,
        eventId: String,
        onPaymentComplete: () -> Unit
    ) {
        _uiState.value = _uiState.value.copy(
            isProcessingPayment = true,
            paymentError = null,
            paymentSuccess = false
        )

        viewModelScope.launch {
            try {
                Log.d(TAG, "Initiating payment for event: $eventId")

                val service = StripePaymentService(RetrofitClient.apiService, activity)

                service.initiatePayment(
                    eventId = eventId,
                    onComplete = {
                        _uiState.value = _uiState.value.copy(
                            isProcessingPayment = false,
                            paymentSuccess = true
                        )
                        onPaymentComplete()
                    },
                    onFailed = { errorMsg ->
                        _uiState.value = _uiState.value.copy(
                            isProcessingPayment = false,
                            paymentError = errorMsg,
                            paymentSuccess = false
                        )
                        Log.e(TAG, "Payment failed: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessingPayment = false,
                    paymentError = e.message ?: "Unknown error",
                    paymentSuccess = false
                )
                Log.e(TAG, "Error initiating payment", e)
            }
        }
    }

    fun clearPaymentError() {
        _uiState.value = _uiState.value.copy(paymentError = null)
    }

    fun clearPaymentSuccess() {
        _uiState.value = _uiState.value.copy(paymentSuccess = false)
    }
}