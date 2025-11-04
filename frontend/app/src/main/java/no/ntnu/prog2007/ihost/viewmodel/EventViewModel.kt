package no.ntnu.prog2007.ihost.viewmodel

import android.content.Context
import android.net.Uri
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
import no.ntnu.prog2007.ihost.data.remote.EventImage
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

data class EventUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedEvent: Event? = null,
    val eventImages: Map<String, List<EventImage>> = emptyMap() // Map of eventId to list of images
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
                        events = events, isLoading = false
                    )
                }
                Log.d("EventViewModel", "Loaded ${events.size} events")

                // Load images for all events
                events.forEach { event ->
                    loadEventImages(event.id)
                }
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

    /**
     * Load images for a specific event
     * @param eventId The ID of the event to load images for
     */
    fun loadEventImages(eventId: String) {
        viewModelScope.launch {
            try {
                Log.d("EventViewModel", "Starting to load images for event: $eventId")
                val images = RetrofitClient.apiService.getEventImages(eventId)
                Log.d("EventViewModel", "Loaded ${images.size} images for event: $eventId")

                // Log each image URL
                images.forEachIndexed { index, image ->
                    Log.d("EventViewModel", "Image $index for event $eventId: ${image.path}")
                }

                _uiState.update { state ->
                    state.copy(
                        eventImages = state.eventImages + (eventId to images)
                    )
                }

                Log.d("EventViewModel", "Updated UI state with images for event: $eventId")
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading images for event $eventId: ${e.message}", e)
                e.printStackTrace()
                // Don't update error state as this is a background operation
            }
        }
    }

    /**
     * Get the first image URL for an event, or null if no images exist
     * @param eventId The ID of the event
     * @return The URL of the first image, or null
     */
    fun getFirstImageUrl(eventId: String): String? {
        return _uiState.value.eventImages[eventId]?.firstOrNull()?.path
    }

    /**
     * Upload an image to Cloudinary
     * @param context Android context for accessing content resolver
     * @param imageUri URI of the image to upload
     * @param eventId Event ID to associate with the image (required)
     * @return The Cloudinary URL of the uploaded image, or null if upload fails
     */
    suspend fun uploadImage(context: Context, imageUri: Uri, eventId: String): String? {
        return try {
            Log.d("EventViewModel", "Starting image upload for URI: $imageUri, eventId: $eventId")

            // Get input stream from URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw IllegalArgumentException("Cannot open image URI")

            // Create a temporary file
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            // Create multipart request body
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // Create eventId request body (required)
            val eventIdBody = eventId.toRequestBody("text/plain".toMediaTypeOrNull())

            // Upload to backend
            val response = RetrofitClient.apiService.uploadImage(body, eventIdBody)

            // Clean up temporary file
            file.delete()

            Log.d("EventViewModel", "Image uploaded successfully: ${response.imageUrl}")
            response.imageUrl
        } catch (e: Exception) {
            Log.e("EventViewModel", "Error uploading image: ${e.message}", e)
            null
        }
    }

    fun createEvent(
        context: Context,
        title: String,
        description: String?,
        eventDate: String,
        eventTime: String?,
        location: String?,
        free: Boolean = true,
        price: Double = 0.0,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Create event first without image
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
                Log.d("EventViewModel", "Event created: ${newEvent.title} with ID: ${newEvent.id}")

                // Upload image after event is created, if provided
                if (imageUri != null) {
                    Log.d("EventViewModel", "Uploading image for event: ${newEvent.id}")
                    val imageUrl = uploadImage(context, imageUri, newEvent.id)
                    if (imageUrl != null) {
                        Log.d("EventViewModel", "Image uploaded successfully: $imageUrl")
                        // Reload images for this event after upload
                        loadEventImages(newEvent.id)
                    } else {
                        Log.w("EventViewModel", "Image upload failed, but event was created")
                    }
                }

                _uiState.update { state ->
                    state.copy(
                        events = state.events + newEvent, isLoading = false
                    )
                }
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
                        }, isLoading = false
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
                        }, isLoading = false
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
                    val updatedEvents =
                        if (state.events.any { it.id == event.id }) { // Event already exists
                            state.events // No change
                        } else { // Event didnt already exist
                            state.events + event // Add new event
                        }
                    state.copy( // Update state with new event
                        events = updatedEvents, isLoading = false
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
