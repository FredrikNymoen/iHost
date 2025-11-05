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
import no.ntnu.prog2007.ihost.data.model.*
import no.ntnu.prog2007.ihost.data.remote.EventImage
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

data class EventUiState(
    val events: List<EventWithMetadata> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedEvent: EventWithMetadata? = null,
    val eventImages: Map<String, List<EventImage>> = emptyMap(), // Map of eventId to list of images
    val eventAttendees: Map<String, List<EventUser>> = emptyMap() // Map of eventId to list of attendees
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

                // Load images and attendees for all events
                events.forEach { eventWithMetadata ->
                    loadEventImages(eventWithMetadata.id)
                    loadEventAttendees(eventWithMetadata.id)
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
                val images = RetrofitClient.apiService.getEventImages(eventId)
                _uiState.update { state ->
                    state.copy(
                        eventImages = state.eventImages + (eventId to images)
                    )
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading images for event $eventId: ${e.message}", e)
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
     * Load attendees for a specific event
     * @param eventId The ID of the event to load attendees for
     * Note: This loads ALL event_users (including PENDING) for the event
     */
    fun loadEventAttendees(eventId: String) {
        viewModelScope.launch {
            try {
                // Get all event_users for this event (no status filter)
                val attendees = RetrofitClient.apiService.getEventAttendees(eventId, status = null)
                _uiState.update { state ->
                    state.copy(
                        eventAttendees = state.eventAttendees + (eventId to attendees)
                    )
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading attendees for event $eventId: ${e.message}", e)
            }
        }
    }

    /**
     * Get attendee count for an event
     * @param eventId The ID of the event
     * @return The number of accepted attendees (ACCEPTED or CREATOR status only)
     */
    fun getAttendeeCount(eventId: String): Int {
        return _uiState.value.eventAttendees[eventId]
            ?.count { it.status == "ACCEPTED" || it.status == "CREATOR" }
            ?: 0
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
                val newEventWithMetadata = RetrofitClient.apiService.createEvent(request)
                Log.d("EventViewModel", "Event created: ${newEventWithMetadata.event.title} with ID: ${newEventWithMetadata.id}")

                // Upload image after event is created, if provided
                if (imageUri != null) {
                    Log.d("EventViewModel", "Uploading image for event: ${newEventWithMetadata.id}")
                    val imageUrl = uploadImage(context, imageUri, newEventWithMetadata.id)
                    if (imageUrl != null) {
                        Log.d("EventViewModel", "Image uploaded successfully: $imageUrl")
                        // Reload images for this event after upload
                        loadEventImages(newEventWithMetadata.id)
                    } else {
                        Log.w("EventViewModel", "Image upload failed, but event was created")
                    }
                }

                _uiState.update { state ->
                    state.copy(
                        events = state.events + newEventWithMetadata, isLoading = false
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

    // Join/Leave functionality removed - managed through event_users now
    // Use acceptInvitation/declineInvitation in EventUserController instead

    /**
     * Fetch an event by its share code and add it to the list of events if not already present.
     * Calls onSuccess callback with the fetched event upon successful retrieval.
     * @param shareCode The share code of the event to fetch.
     * @param onSuccess Callback invoked with the fetched EventWithMetadata on success.
     */
    fun getEventByCode(shareCode: String, onSuccess: (EventWithMetadata) -> Unit) {
        viewModelScope.launch { // Launch coroutine for network call
            _uiState.update { it.copy(isLoading = true, errorMessage = null) } // Set loading state
            try { // Try to fetch event
                // Fetch event by share code - backend will auto-create PENDING event_user if needed
                val eventWithMetadata = RetrofitClient.apiService.getEventByCode(shareCode)

                // Reload all events to get the updated list from server (including new event_user)
                loadEvents()

                // Success callback and log it
                onSuccess(eventWithMetadata)
                Log.d("EventViewModel", "Fetched event '${eventWithMetadata.event.title}' by code: $shareCode")
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

    /**
     * Accept an event invitation
     * @param eventUserId The ID of the event_user document
     * @param onSuccess Callback invoked on successful acceptance
     * @param onError Callback invoked on error
     */
    fun acceptInvitation(eventUserId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.acceptInvitation(eventUserId)
                loadEvents() // Reload events to get updated status
                onSuccess()
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error accepting invitation: ${e.message}", e)
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Decline an event invitation
     * @param eventUserId The ID of the event_user document
     * @param onSuccess Callback invoked on successful decline
     * @param onError Callback invoked on error
     */
    fun declineInvitation(eventUserId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.declineInvitation(eventUserId)
                loadEvents() // Reload events to get updated status
                onSuccess()
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error declining invitation: ${e.message}", e)
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Get user Username by UID
     * @param uid The user's UID
     * @return The user's Username, or "User" if fetch fails
     */
    suspend fun getUserUserName(uid: String): String {
        return try {
            val user = RetrofitClient.apiService.getUserByUid(uid)
            user.username
        } catch (e: Exception) {
            Log.e("EventViewModel", "Error fetching user name for $uid: ${e.message}", e)
            "User"
        }
    }

    /**
     * Get all users
     * @return List of all users
     */
    suspend fun getAllUsers(): List<User> {
        return try {
            RetrofitClient.apiService.getAllUsers()
        } catch (e: Exception) {
            Log.e("EventViewModel", "Error fetching all users: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Invite users to an event
     * @param eventId The event ID
     * @param userIds List of user IDs to invite
     * @param onSuccess Callback invoked on successful invitation
     * @param onError Callback invoked on error
     */
    fun inviteUsers(eventId: String, userIds: List<String>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = InviteUsersRequest(eventId = eventId, userIds = userIds)
                RetrofitClient.apiService.inviteUsers(request)
                loadEventAttendees(eventId) // Reload attendees after inviting
                onSuccess()
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error inviting users: ${e.message}", e)
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Reset all event data (call this when user logs out)
     */
    fun resetEvents() {
        _uiState.update {
            EventUiState() // Reset to initial empty state
        }
        eventsLoaded = false
    }
}
