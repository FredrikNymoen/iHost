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

    private var eventsLoaded = false

    fun ensureEventsLoaded() {
        if (!eventsLoaded) {
            loadEvents()
            eventsLoaded = true
        }
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
        location: String?,
        free: Boolean = true,
        price: Double = 0.0
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val request = CreateEventRequest(
                    title = title,
                    description = description,
                    eventDate = eventDate,
                    eventTime = eventTime,
                    location = location,
                    free = free,
                    price = price
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

    /**
     * Fetch an event by its share code and add it to the list of events if not already present.
     * Calls onSuccess callback with the fetched event upon successful retrieval.
     * @param shareCode The share code of the event to fetch.
     * @param onSuccess Callback invoked with the fetched Event on success.
     */
    fun getEventByCode(shareCode: String, onSuccess: (Event) -> Unit) {
        viewModelScope.launch { // Launch coroutine for network call
            _uiState.update { it.copy(isLoading = true, errorMessage = null) } // Set loading state
            try { // Try to fetch event
                // Fetch event by share code
                val event = RetrofitClient.apiService.getEventByCode(shareCode)
                _uiState.update { state ->
                    // Only add event if not already present
                    val updatedEvents = if (state.events.any { it.id == event.id }) { // Event already exists
                        state.events // No change
                    } else { // Event didnt already exist
                        state.events + event // Add new event
                    }
                    state.copy( // Update state with new event
                        events = updatedEvents,
                        isLoading = false
                    )
                }
                // Success callback and log it
                onSuccess(event)
                Log.d("EventViewModel", "Fetched event '${event.title}' by code: $shareCode")
            } catch (e: Exception) { // Handle errors
                Log.e("EventViewModel", "Error fetching event by code: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Feil ved henting av event: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
