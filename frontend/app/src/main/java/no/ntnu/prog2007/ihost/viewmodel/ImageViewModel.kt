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
import no.ntnu.prog2007.ihost.data.model.domain.EventImage
import no.ntnu.prog2007.ihost.data.repository.ImageRepository
import java.io.File
import java.io.FileOutputStream

data class ImageUiState(
    val eventImages: Map<String, List<EventImage>> = emptyMap(),
    val isUploading: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastUploadedUrl: String? = null
)

/**
 * ViewModel for image operations
 * Handles image uploads for events and profiles, and loading event images
 */
class ImageViewModel : ViewModel() {

    private val imageRepository = ImageRepository()

    private val _uiState = MutableStateFlow(ImageUiState())
    val uiState: StateFlow<ImageUiState> = _uiState.asStateFlow()

    /**
     * Upload an image for an event
     * @param context Android context for accessing content resolver
     * @param imageUri URI of the image to upload
     * @param eventId Event ID to associate with the image
     * @param onSuccess Callback invoked with the uploaded image URL
     * @param onError Callback invoked on error
     */
    fun uploadEventImage(
        context: Context,
        imageUri: Uri,
        eventId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null, lastUploadedUrl = null) }

            try {
                Log.d("ImageViewModel", "Starting event image upload for URI: $imageUri, eventId: $eventId")

                val file = uriToFile(context, imageUri)

                imageRepository.uploadEventImage(file, eventId).fold(
                    onSuccess = { imageUrl ->
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                lastUploadedUrl = imageUrl
                            )
                        }
                        Log.d("ImageViewModel", "Event image uploaded successfully: $imageUrl")

                        // Reload images for this event
                        loadEventImages(eventId)

                        onSuccess(imageUrl)
                    },
                    onFailure = { error ->
                        val errorMsg = error.message ?: "Failed to upload image"
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = errorMsg
                            )
                        }
                        Log.e("ImageViewModel", "Error uploading event image", error)
                        onError(errorMsg)
                    }
                )

                // Clean up temporary file
                file.delete()

            } catch (e: Exception) {
                val errorMsg = e.message ?: "Failed to prepare image for upload"
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = errorMsg
                    )
                }
                Log.e("ImageViewModel", "Error preparing image upload", e)
                onError(errorMsg)
            }
        }
    }

    /**
     * Upload a profile photo for the current user
     * @param context Android context for accessing content resolver
     * @param imageUri URI of the profile photo to upload
     * @param onSuccess Callback invoked with the uploaded photo URL
     * @param onError Callback invoked on error
     */
    fun uploadProfilePhoto(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null, lastUploadedUrl = null) }

            try {
                Log.d("ImageViewModel", "Starting profile photo upload for URI: $imageUri")

                val file = uriToFile(context, imageUri)

                imageRepository.uploadProfilePhoto(file).fold(
                    onSuccess = { photoUrl ->
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                lastUploadedUrl = photoUrl
                            )
                        }
                        Log.d("ImageViewModel", "Profile photo uploaded successfully: $photoUrl")
                        onSuccess(photoUrl)
                    },
                    onFailure = { error ->
                        val errorMsg = error.message ?: "Failed to upload profile photo"
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = errorMsg
                            )
                        }
                        Log.e("ImageViewModel", "Error uploading profile photo", error)
                        onError(errorMsg)
                    }
                )

                // Clean up temporary file
                file.delete()

            } catch (e: Exception) {
                val errorMsg = e.message ?: "Failed to prepare profile photo for upload"
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = errorMsg
                    )
                }
                Log.e("ImageViewModel", "Error preparing profile photo upload", e)
                onError(errorMsg)
            }
        }
    }

    /**
     * Load images for a specific event
     * @param eventId The ID of the event to load images for
     */
    fun loadEventImages(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            imageRepository.getEventImages(eventId).fold(
                onSuccess = { images ->
                    _uiState.update { state ->
                        state.copy(
                            eventImages = state.eventImages + (eventId to images),
                            isLoading = false
                        )
                    }
                    Log.d("ImageViewModel", "Loaded ${images.size} images for event $eventId")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load images"
                        )
                    }
                    Log.e("ImageViewModel", "Error loading images for event $eventId", error)
                }
            )
        }
    }

    /**
     * Get images for a specific event from the current state
     * @param eventId The ID of the event
     * @return List of images for the event, or empty list if none
     */
    fun getEventImages(eventId: String): List<EventImage> {
        return _uiState.value.eventImages[eventId] ?: emptyList()
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
     * Convert URI to File for upload
     * Creates a temporary file in cache directory
     */
    private fun uriToFile(context: Context, imageUri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw IllegalArgumentException("Cannot open image URI")

        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()

        return file
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Clear last uploaded URL
     */
    fun clearLastUploadedUrl() {
        _uiState.update { it.copy(lastUploadedUrl = null) }
    }
}
