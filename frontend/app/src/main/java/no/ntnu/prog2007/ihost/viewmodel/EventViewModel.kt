package no.ntnu.prog2007.ihost.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.ntnu.prog2007.ihost.data.model.CreateEventRequest
import no.ntnu.prog2007.ihost.data.model.Event
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient

data class EventUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedEvent: Event? = null
)

class EventViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val events = RetrofitClient.apiService.getAllEvents()
                _uiState.update {
                    it.copy(
                        events = events,
                        isLoading = false
                    )
                }
                Log.d("EventViewModel", "Loaded ${events.size} events")
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading events: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Feil ved lasting av events: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun createEvent(
        title: String,
        description: String?,
        eventDate: String,
        eventTime: String?,
        location: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val request = CreateEventRequest(
                    title = title,
                    description = description,
                    eventDate = eventDate,
                    eventTime = eventTime,
                    location = location
                )
                val newEvent = RetrofitClient.apiService.createEvent(request)
                _uiState.update { state ->
                    state.copy(
                        events = state.events + newEvent,
                        isLoading = false
                    )
                }
                Log.d("EventViewModel", "Event created: ${newEvent.title}")
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error creating event: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Feil ved opprettelse av event: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                RetrofitClient.apiService.deleteEvent(eventId)
                _uiState.update { state ->
                    state.copy(
                        events = state.events.filter { event -> event.id != eventId },
                        isLoading = false
                    )
                }
                Log.d("EventViewModel", "Event deleted: $eventId")
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error deleting event: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Feil ved sletting av event: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun joinEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val updatedEvent = RetrofitClient.apiService.joinEvent(eventId)
                _uiState.update { state ->
                    state.copy(
                        events = state.events.map { event ->
                            if (event.id == eventId) updatedEvent else event
                        },
                        isLoading = false
                    )
                }
                Log.d("EventViewModel", "Joined event: $eventId")
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error joining event: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Feil ved joining av event: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun leaveEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val updatedEvent = RetrofitClient.apiService.leaveEvent(eventId)
                _uiState.update { state ->
                    state.copy(
                        events = state.events.map { event ->
                            if (event.id == eventId) updatedEvent else event
                        },
                        isLoading = false
                    )
                }
                Log.d("EventViewModel", "Left event: $eventId")
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error leaving event: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Feil ved leaving av event: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
