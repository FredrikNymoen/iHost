package no.ntnu.prog2007.ihost.ui.screens.events.editevent

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.android.gms.maps.model.LatLng
import no.ntnu.prog2007.ihost.ui.components.TopBar
import no.ntnu.prog2007.ihost.ui.components.event.EventImageSection
import no.ntnu.prog2007.ihost.ui.components.event.ImageSourceDialog
import no.ntnu.prog2007.ihost.ui.components.event.LocationPickerDialog
import no.ntnu.prog2007.ihost.ui.components.event.TimePickerDialog
import no.ntnu.prog2007.ihost.ui.screens.events.editevent.components.EditEventDetailsForm
import no.ntnu.prog2007.ihost.ui.screens.events.editevent.components.UpdateEventButton
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: String,
    viewModel: EventViewModel,
    onBack: () -> Unit,
    onEventUpdated: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    // Get the event from state
    val eventWithMetadata = uiState.events.find { it.id == eventId }
    val event = eventWithMetadata?.event

    // Pre-fill form with existing event data
    var title by remember { mutableStateOf(event?.title ?: "") }
    var description by remember { mutableStateOf(event?.description ?: "") }
    var eventDate by remember { mutableStateOf(event?.eventDate ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)) }
    var eventTime by remember { mutableStateOf(event?.eventTime ?: "") }
    var location by remember { mutableStateOf(event?.location ?: "") }
    var locationLatLng by remember { mutableStateOf<LatLng?>(null) }
    var showLocationPicker by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var imageKey by remember { mutableStateOf(0) }

    // Update form when event loads
    LaunchedEffect(event) {
        event?.let {
            title = it.title
            description = it.description ?: ""
            eventDate = it.eventDate
            eventTime = it.eventTime ?: ""
            location = it.location ?: ""
        }
    }

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
            val imageFile = File(
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

    // Dialog for selecting image source
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
            initialSelectedDateMillis = LocalDate.parse(eventDate).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        eventDate = selectedDate.format(DateTimeFormatter.ISO_DATE)
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
            initialHour = if (eventTime.isNotEmpty()) eventTime.split(":")[0].toIntOrNull() ?: 0 else 0,
            initialMinute = if (eventTime.isNotEmpty()) eventTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0 else 0,
            is24Hour = true
        )

        TimePickerDialog(
            onDismissRequest = { showTimePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    eventTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
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
                showLocationPicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopBar(
                title = { Text("Edit Event") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            EventImageSection(
                selectedImageUri = selectedImageUri,
                imageKey = imageKey,
                placeholderText = "Tap to change event image",
                onAddImageClick = { showImageSourceDialog = true },
                onRemoveImage = { selectedImageUri = null }
            )

            EditEventDetailsForm(
                title = title,
                description = description,
                eventDate = eventDate,
                eventTime = eventTime,
                location = location,
                isLoading = uiState.isLoading,
                onTitleChange = { title = it },
                onDescriptionChange = { description = it },
                onDateClick = { showDatePickerDialog = true },
                onTimeClick = { showTimePickerDialog = true },
                onLocationClick = { showLocationPicker = true }
            )

            UpdateEventButton(
                enabled = title.isNotEmpty() && eventDate.isNotEmpty(),
                isLoading = uiState.isLoading,
                onClick = {
                    viewModel.updateEvent(
                        eventId = eventId,
                        title = title,
                        description = description.ifEmpty { null },
                        eventDate = eventDate,
                        eventTime = eventTime.ifEmpty { null },
                        location = location.ifEmpty { null },
                        imageUri = selectedImageUri
                    )
                    onEventUpdated()
                }
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
