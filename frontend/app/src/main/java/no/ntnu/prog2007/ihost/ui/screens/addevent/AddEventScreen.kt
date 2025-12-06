package no.ntnu.prog2007.ihost.ui.screens.addevent

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.android.gms.maps.model.LatLng
import no.ntnu.prog2007.ihost.ui.components.events.EventDetailsForm
import no.ntnu.prog2007.ihost.ui.components.events.EventImageSection
import no.ntnu.prog2007.ihost.ui.components.events.ImageSourceDialog
import no.ntnu.prog2007.ihost.ui.components.events.LocationPickerDialog
import no.ntnu.prog2007.ihost.ui.components.events.TimePickerDialog
import no.ntnu.prog2007.ihost.ui.screens.addevent.components.CreateEventButton
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Create new event screen
 *
 * Form for creating a new event with:
 * - Basic info: title, description, date, time
 * - Location picker with Google Maps integration
 * - Image upload from camera or gallery
 * - Free/paid event toggle
 *
 * Validates that date/time is in the future before allowing creation.
 * On successful creation, navigates back to Events screen.
 *
 * @param viewModel EventViewModel for event creation
 * @param onEventCreated Callback invoked after successful event creation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    viewModel: EventViewModel,
    onEventCreated: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Norwegian timezone
    val norwegianZone = ZoneId.of("Europe/Oslo")
    val nowInNorway = LocalDateTime.now(norwegianZone)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(nowInNorway.toLocalDate().format(DateTimeFormatter.ISO_DATE)) }
    var eventTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var locationLatLng by remember { mutableStateOf<LatLng?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var isFree by remember { mutableStateOf(true) }
    var dateTimeError by remember { mutableStateOf<String?>(null) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var imageKey by remember { mutableStateOf(0) }
    var showLocationPicker by remember { mutableStateOf(false) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) {
            selectedImageUri = null
            Log.d("Camera", "Photo capture cancelled or failed")
        } else {
            Log.d("Camera", "Photo captured: $selectedImageUri")
            imageKey++
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val imageFile = java.io.File(
                context.cacheDir,
                "camera_${System.currentTimeMillis()}.jpg"
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile
            )
            selectedImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            Log.d("Camera", "Camera permission denied")
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Log.d("PhotoPicker", "Selected URI: $uri")
        }
    }

    // Image Source Dialog
    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onTakePhoto = {
                showImageSourceDialog = false
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onSelectFromGallery = {
                showImageSourceDialog = false
                galleryLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        )
    }

    // DatePicker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.parse(eventDate).atStartOfDay(norwegianZone).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val selectedDate = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(norwegianZone)
                        .toLocalDate()
                    val todayInNorway = nowInNorway.toLocalDate()
                    return !selectedDate.isBefore(todayInNorway)
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(norwegianZone)
                            .toLocalDate()
                        eventDate = selectedDate.format(DateTimeFormatter.ISO_DATE)

                        if (eventTime.isNotEmpty()) {
                            val eventDateTime = LocalDateTime.of(selectedDate, LocalTime.parse(eventTime))
                            if (eventDateTime.isBefore(nowInNorway)) {
                                dateTimeError = "Event time cannot be in the past"
                            } else {
                                dateTimeError = null
                            }
                        }
                    }
                    showDatePickerDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePicker Dialog
    if (showTimePickerDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = if (eventTime.isNotEmpty()) eventTime.split(":")[0].toIntOrNull() ?: nowInNorway.hour else nowInNorway.hour,
            initialMinute = if (eventTime.isNotEmpty()) eventTime.split(":").getOrNull(1)?.toIntOrNull() ?: nowInNorway.minute else nowInNorway.minute,
            is24Hour = true
        )

        TimePickerDialog(
            onDismissRequest = { showTimePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    val selectedDate = LocalDate.parse(eventDate)
                    val eventDateTime = LocalDateTime.of(selectedDate, LocalTime.parse(selectedTime))

                    if (eventDateTime.isBefore(nowInNorway)) {
                        dateTimeError = "Event time cannot be in the past"
                    } else {
                        eventTime = selectedTime
                        dateTimeError = null
                    }
                    showTimePickerDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    // LocationPicker Dialog
    if (showLocationPicker) {
        LocationPickerDialog(
            initialLocation = location,
            onDismiss = { showLocationPicker = false },
            onLocationSelected = { selectedLocation, latLng ->
                location = selectedLocation
                locationLatLng = latLng
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        EventImageSection(
            selectedImageUri = selectedImageUri,
            imageKey = imageKey,
            onAddImageClick = { showImageSourceDialog = true },
            onRemoveImage = { selectedImageUri = null }
        )

        EventDetailsForm(
            title = title,
            onTitleChange = { title = it },
            description = description,
            onDescriptionChange = { description = it },
            eventDate = eventDate,
            onDateClick = { showDatePickerDialog = true },
            eventTime = eventTime,
            onTimeClick = { showTimePickerDialog = true },
            location = location,
            onLocationClick = { showLocationPicker = true },
            isLoading = uiState.isLoading,
            showLocationError = true
        )

        if (dateTimeError != null) {
            Text(
                text = dateTimeError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        CreateEventButton(
            onClick = {
                val selectedDate = LocalDate.parse(eventDate)
                val eventDateTime = if (eventTime.isNotEmpty()) {
                    LocalDateTime.of(selectedDate, LocalTime.parse(eventTime))
                } else {
                    selectedDate.atStartOfDay()
                }

                if (eventDateTime.isBefore(nowInNorway)) {
                    dateTimeError = "Event time cannot be in the past"
                    return@CreateEventButton
                }

                val priceValue = 0.0
                viewModel.createEvent(
                    context = context,
                    title = title,
                    description = description.ifEmpty { null },
                    eventDate = eventDate,
                    eventTime = eventTime.ifEmpty { null },
                    location = location.ifEmpty { null },
                    free = isFree,
                    price = priceValue,
                    imageUri = selectedImageUri
                )
                onEventCreated()
            },
            enabled = title.isNotEmpty()
                    && eventDate.isNotEmpty()
                    && dateTimeError == null
                    && !uiState.isLoading,
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}
