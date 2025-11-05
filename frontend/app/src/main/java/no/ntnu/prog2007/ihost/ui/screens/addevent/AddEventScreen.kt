package no.ntnu.prog2007.ihost.ui.screens.addevent

import coil3.compose.AsyncImage
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddEventScreen(
    viewModel: EventViewModel,
    onEventCreated: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_DATE)) }
    var eventTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var isFree by remember { mutableStateOf(true) }
    var price by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var imageKey by remember { mutableStateOf(0) } // Key to force image reload

    // Camera launcher - must be declared before cameraPermissionLauncher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) {
            selectedImageUri = null
            Log.d("Camera", "Photo capture cancelled or failed")
        } else {
            Log.d("Camera", "Photo captured: $selectedImageUri")
            imageKey++ // Increment key to force image reload
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, now take photo
            val imageFile = java.io.File(
                context.cacheDir,
                "camera_${System.currentTimeMillis()}.jpg"
            )
            val uri = androidx.core.content.FileProvider.getUriForFile(
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
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = {
                Text(
                    "Select Image Source",
                    color = Color(0xFFFFC107),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "How would you like to add an image?",
                        color = Color.White
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            // Request camera permission first
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0C5CA7),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Take photo",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo")
                    }
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107),
                            contentColor = Color(0xFF001D3D)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Select from gallery",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImageSourceDialog = false }
                ) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF001D3D)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(150.dp)
                .background(
                    color = Color(0xFF4A90E2),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFFFFC107),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                androidx.compose.runtime.key(imageKey) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected event image",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color(0xFF4A90E2),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = Color(0xFFFFC107),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                IconButton(
                    onClick = { selectedImageUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Red, RoundedCornerShape(50))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove image",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = { showImageSourceDialog = true })
                ) {

                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = "Add image",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFFFFC107)
                    )

                    Text(
                        "Tap to add image",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Event tittel", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .padding(bottom = 16.dp),
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Beskrivelse (valgfritt)", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .heightIn(min = 80.dp),
            minLines = 3,
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        OutlinedTextField(
            value = eventDate,
            onValueChange = { eventDate = it },
            label = { Text("Dato (YYYY-MM-DD)", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        OutlinedTextField(
            value = eventTime,
            onValueChange = { eventTime = it },
            label = { Text("Tid (HH:mm) (valgfritt)", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Sted (valgfritt)", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Gratis event", color = Color.White)
            Switch(
                checked = isFree,
                onCheckedChange = { isFree = it },
                enabled = !uiState.isLoading,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (!isFree) {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Pris (kr)", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !uiState.isLoading,
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFC107),
                    unfocusedBorderColor = Color(0xFFFFC107),
                    cursorColor = Color(0xFFFFC107)
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                )
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val priceValue = if (!isFree && price.isNotEmpty()) {
                        price.toDoubleOrNull() ?: 0.0
                    } else {
                        0.0
                    }
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
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                enabled = title.isNotEmpty() && eventDate.isNotEmpty() && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color(0xFF001D3D),
                    disabledContainerColor = Color(0xFFB8860B),
                    disabledContentColor = Color(0xFF001D3D)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 12.dp,
                    pressedElevation = 6.dp
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = Color(0xFF001D3D),
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Event",
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        "Opprett event",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
