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
import no.ntnu.prog2007.ihost.data.model.domain.EventUser
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.data.model.domain.EventImage
import no.ntnu.prog2007.ihost.data.model.domain.EventWithMetadata
import no.ntnu.prog2007.ihost.data.repository.EventRepository
import no.ntnu.prog2007.ihost.data.repository.EventUserRepository
import no.ntnu.prog2007.ihost.data.repository.ImageRepository
import no.ntnu.prog2007.ihost.data.repository.UserRepository

data class EventUiState(
    val events: List<EventWithMetadata> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedEvent: EventWithMetadata? = null,
    val eventImages: Map<String, List<EventImage>> = emptyMap(), // Map of eventId to list of images
    val eventAttendees: Map<String, List<EventUser>> = emptyMap() // Map of eventId to list of attendees
)

class EventViewModel: ViewModel() {

    private val eventRepository = EventRepository()
    private val eventUserRepository = EventUserRepository()
    private val imageRepository = ImageRepository()
    private val userRepository = UserRepository()

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

            eventRepository.getUserEvents().fold(
                onSuccess = { events ->
                    _uiState.update {
                        it.copy(
                            events = events,
                            isLoading = false
                        )
                    }
                    Log.d("EventViewModel", "Loaded ${events.size} events")

                    // Load images and attendees for all events
                    events.forEach { eventWithMetadata ->
                        loadEventImages(eventWithMetadata.id)
                        loadEventAttendees(eventWithMetadata.id)
                    }
                },
                onFailure = { error ->
                    Log.e("EventViewModel", "Error loading events: ${error.message}", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Feil ved lasting av events: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Load images for a specific event
     * @param eventId The ID of the event to load images for
     */
    fun loadEventImages(eventId: String) {
        viewModelScope.launch {
            imageRepository.getEventImages(eventId).fold(
                onSuccess = { images ->
                    _uiState.update { state ->
                        state.copy(
                            eventImages = state.eventImages + (eventId to images)
                        )
                    }
                },
                onFailure = { error ->
                    Log.e("EventViewModel", "Error loading images for event $eventId: ${error.message}", error)
                    // Don't update error state as this is a background operation
                }
            )
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
            eventUserRepository.getEventAttendees(eventId, status = null).fold(
                onSuccess = { attendees ->
                    _uiState.update { state ->
                        state.copy(
                            eventAttendees = state.eventAttendees + (eventId to attendees)
                        )
                    }
                },
                onFailure = { error ->
                    Log.e("EventViewModel", "Error loading attendees for event $eventId: ${error.message}", error)
                }
            )
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
     * Upload an image for an event
     * @param context Android context for accessing content resolver
     * @param imageUri URI of the image to upload
     * @param eventId Event ID to associate with the image
     * @return The uploaded image URL, or null if upload fails
     */
    private suspend fun uploadEventImage(context: Context, imageUri: Uri, eventId: String): String? {
        return try {
            Log.d("EventViewModel", "Starting event image upload for URI: $imageUri, eventId: $eventId")

            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw IllegalArgumentException("Cannot open image URI")

            val file = java.io.File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            java.io.FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            imageRepository.uploadEventImage(file, eventId).fold(
                onSuccess = { imageUrl ->
                    file.delete()
                    Log.d("EventViewModel", "Event image uploaded successfully: $imageUrl")
                    imageUrl
                },
                onFailure = { error ->
                    file.delete()
                    Log.e("EventViewModel", "Error uploading event image: ${error.message}", error)
                    null
                }
            )
        } catch (e: Exception) {
            Log.e("EventViewModel", "Error preparing event image upload: ${e.message}", e)
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

            eventRepository.createEvent(
                title = title,
                description = description,
                eventDate = eventDate,
                eventTime = eventTime,
                location = location,
                free = free,
                price = price
            ).fold(
                onSuccess = { newEventWithMetadata ->
                    Log.d("EventViewModel", "Event created: ${newEventWithMetadata.event.title} with ID: ${newEventWithMetadata.id}")

                    // Upload image after event is created, if provided
                    if (imageUri != null) {
                        Log.d("EventViewModel", "Uploading image for event: ${newEventWithMetadata.id}")
                        val imageUrl = uploadEventImage(context, imageUri, newEventWithMetadata.id)
                        if (imageUrl != null) {
                            Log.d("EventViewModel", "Image uploaded successfully: $imageUrl")
                            loadEventImages(newEventWithMetadata.id)
                        } else {
                            Log.w("EventViewModel", "Image upload failed, but event was created")
                        }
                    }

                    _uiState.update { state ->
                        state.copy(
                            events = state.events + newEventWithMetadata,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    Log.e("EventViewModel", "Error creating event: ${error.message}", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Feil ved opprettelse av event: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }
    fun editEvent(eventId:String, onSuccess: (EventWithMetadata) -> Unit){

    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            eventRepository.deleteEvent(eventId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            events = state.events.filter { event -> event.id != eventId },
                            isLoading = false
                        )
                    }
                    Log.d("EventViewModel", "Event deleted: $eventId")
                },
                onFailure = { error ->
                    Log.e("EventViewModel", "Error deleting event: ${error.message}", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Feil ved sletting av event: ${error.localizedMessage}"
                        )
                    }
                }
            )
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            eventRepository.getEventByCode(shareCode).fold(
                onSuccess = { eventWithMetadata ->
                    // Reload all events to get the updated list from server (including new event_user)
                    loadEvents()

                    // Success callback and log it
                    onSuccess(eventWithMetadata)
                    Log.d("EventViewModel", "Fetched event '${eventWithMetadata.event.title}' by code: $shareCode")
                },
                onFailure = { error ->
                    Log.e("EventViewModel", "Error fetching event by code: ${error.message}", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Feil ved henting av event: ${error.localizedMessage}"
                        )
                    }
                }
            )
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
            eventUserRepository.acceptInvitation(eventUserId).fold(
                onSuccess = {
                    loadEvents() // Reload events to get updated status
                    onSuccess()
                },
                onFailure = { error ->
                    Log.e("EventViewModel", "Error accepting invitation: ${error.message}", error)
                    onError(error.localizedMessage ?: "Unknown error")
                }
            )
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
            eventUserRepository.declineInvitation(eventUserId).fold(
                onSuccess = {
                    loadEvents() // Reload events to get updated status
                    onSuccess()
                },
                onFailure = { error ->
                    Log.e("EventViewModel", "Error declining invitation: ${error.message}", error)
                    onError(error.localizedMessage ?: "Unknown error")
                }
            )
        }
    }

    /**
     * Get user Username by UID
     * @param uid The user's UID
     * @return The user's Username, or "User" if fetch fails
     */
    suspend fun getUserUserName(uid: String): String {
        return userRepository.getUserByUid(uid).fold(
            onSuccess = { user ->
                print(user.username)
                user.username
            },
            onFailure = { error ->
                Log.e("EventViewModel", "Error fetching user name for $uid: ${error.message}", error)
                "User"
            }
        )
    }

    /**
     * Get user Last name by UID
     * @param uid The user's UID
     * @return The user's Last name, or "User" if fetch fails
     */
    suspend fun getUserLastName(uid: String): String? {
        return userRepository.getUserByUid(uid).fold(
            onSuccess = { user -> user.lastName },
            onFailure = { error ->
                Log.e("EventViewModel", "Error fetching user name for $uid: ${error.message}", error)
                "User"
            }
        )
    }

    /**
     * Get user First name by UID
     * @param uid The user's UID
     * @return The user's First name, or "User" if fetch fails
     */
    suspend fun getUserFirstName(uid: String): String {
        return userRepository.getUserByUid(uid).fold(
            onSuccess = { user -> user.firstName },
            onFailure = { error ->
                Log.e("EventViewModel", "Error fetching user name for $uid: ${error.message}", error)
                "User"
            }
        )
    }

    /**
     * Get user by UID
     * @param uid The user's UID
     * @return The User object, or null if fetch fails
     */
    suspend fun getUserByUid(uid: String): User? {
        return userRepository.getUserByUid(uid).fold(
            onSuccess = { user -> user },
            onFailure = { error ->
                Log.e("EventViewModel", "Error fetching user for $uid: ${error.message}", error)
                null
            }
        )
    }

    /**
     * Get all users
     * @return List of all users
     */
    suspend fun getAllUsers(): List<User> {
        return userRepository.getAllUsers().fold(
            onSuccess = { users -> users },
            onFailure = { error ->
                Log.e("EventViewModel", "Error fetching all users: ${error.message}", error)
                emptyList()
            }
        )
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
            eventUserRepository.inviteUsers(eventId, userIds).fold(
                onSuccess = {
                    loadEventAttendees(eventId) // Reload attendees after inviting
                    onSuccess()
                },
                onFailure = { error ->
                    Log.e("EventViewModel", "Error inviting users: ${error.message}", error)
                    onError(error.localizedMessage ?: "Unknown error")
                }
            )
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

    /**
     * Updating information about event
     */
    fun updateEvent(
        eventId: String,
        title: String,
        description: String?,
        eventDate: String,
        eventTime: String?,
        location: String?,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            eventRepository.updateEvent(
                eventId = eventId,
                title = title,
                description = description,
                eventDate = eventDate,
                eventTime = eventTime,
                location = location
            ).fold(
                onSuccess = { updatedEvent ->
                    // Update the events list in state
                    val updatedEvents = _uiState.value.events.map { eventWithMetadata ->
                        if (eventWithMetadata.id == eventId) {
                            updatedEvent
                        } else {
                            eventWithMetadata
                        }
                    }

                    _uiState.update {
                        it.copy(
                            events = updatedEvents,
                            isLoading = false
                        )
                    }
                    Log.d("EventViewModel", "Event updated successfully: $eventId")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to update event: ${error.message}"
                        )
                    }
                    Log.e("EventViewModel", "Error updating event", error)
                }
            )
        }
    }
}
