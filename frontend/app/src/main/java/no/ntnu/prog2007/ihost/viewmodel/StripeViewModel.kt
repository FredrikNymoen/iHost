package no.ntnu.prog2007.ihost.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient

// viewmodel/StripeViewModel.kt
class StripeViewModel(
) : ViewModel() {

    private val _publishableKey = MutableStateFlow<String?>(null)
    val publishableKey: StateFlow<String?> = _publishableKey

    init {
        viewModelScope.launch {
            runCatching { RetrofitClient.apiService.getKeys().publishableKey }
                .onSuccess { _publishableKey.value = it }
                .onFailure { _publishableKey.value = null } // handle error properly
        }
    }
}