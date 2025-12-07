package no.ntnu.prog2007.ihost.ui.components.events

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Performs reverse geocoding to get formatted address from coordinates
 *
 * Converts geographic coordinates to a human-readable address string.
 * Format: "Gatenavn Gatenummer, Postnummer Poststed"
 *
 * @param context Android context for accessing Geocoder
 * @param latLng Geographic coordinates to convert
 * @return Formatted address string or null if geocoding fails
 */
suspend fun getAddressFromLocation(context: Context, latLng: LatLng): String? {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13+ (API 33+)
                var addressResult: String? = null
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        addressResult = formatAddress(address)
                    }
                }
                // Wait a bit for the callback
                delay(1000)
                addressResult
            } else {
                // For older Android versions
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    formatAddress(addresses[0])
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("LocationPicker", "Reverse geocoding failed", e)
            null
        }
    }
}

/**
 * Formats address with preference for full address line
 *
 * This gives better results for locations like NTNU, businesses, etc.
 * Cleans up the address by removing country suffix.
 *
 * @param address Android Address object from Geocoder
 * @return Formatted address string without country suffix
 */
private fun formatAddress(address: Address): String {
    // Get the full first address line (most complete and accurate)
    val fullAddressLine = address.getAddressLine(0)

    // If we have a full address line, use it but clean it up
    if (!fullAddressLine.isNullOrEmpty()) {
        // Remove country from the end if present (usually ", Norway" or ", Norge")
        val cleanedAddress = fullAddressLine
            .replace(", Norway", "")
            .replace(", Norge", "")
            .trim()

        return cleanedAddress
    }

    // Fallback: try to build address manually
    val streetAddress = address.thoroughfare ?: "" // Gatenavn
    val streetNumber = address.subThoroughfare ?: "" // Gatenummer
    val postalCode = address.postalCode ?: "" // Postnummer
    val locality = address.locality ?: "" // Poststed
    val featureName = address.featureName ?: "" // Building/place name

    val parts = mutableListOf<String>()

    // Add feature name if it's different from street name
    if (featureName.isNotEmpty() && featureName != streetAddress) {
        parts.add(featureName)
    }

    // Add street address
    if (streetAddress.isNotEmpty() && streetNumber.isNotEmpty()) {
        parts.add("$streetAddress $streetNumber")
    } else if (streetAddress.isNotEmpty()) {
        parts.add(streetAddress)
    }

    // Add city part
    if (postalCode.isNotEmpty() && locality.isNotEmpty()) {
        parts.add("$postalCode $locality")
    } else if (locality.isNotEmpty()) {
        parts.add(locality)
    }

    return if (parts.isNotEmpty()) {
        parts.joinToString(", ")
    } else {
        "Unknown location"
    }
}

/**
 * Location picker dialog with interactive map
 *
 * Provides a full-screen dialog for selecting event locations using Google Maps.
 * Features include:
 * - Interactive map with draggable marker
 * - Address search with autocomplete predictions (Google Places API)
 * - Reverse geocoding to get address from map clicks
 * - Search result debouncing for better performance
 *
 * @param initialLocation Initial location string to display in search field
 * @param onDismiss Callback invoked when dialog is dismissed
 * @param onLocationSelected Callback invoked when location is confirmed
 *                           Receives formatted address string and LatLng coordinates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    initialLocation: String,
    onDismiss: () -> Unit,
    onLocationSelected: (String, LatLng) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf(initialLocation) }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var showPredictions by remember { mutableStateOf(false) }
    var selectedLatLng by remember { mutableStateOf(LatLng(60.7957, 10.6910))} // Default: Gjøvik
    var markerPosition by remember { mutableStateOf(selectedLatLng) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLatLng, 12f)
    }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // Initialize Places API synchronously before creating client
    if (!Places.isInitialized()) {
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""
            if (apiKey.isNotEmpty() && apiKey != "YOUR_GOOGLE_MAPS_API_KEY_HERE") {
                Places.initialize(context, apiKey)
            } else {
                Log.e("LocationPicker", "Invalid or missing Google Maps API key in AndroidManifest.xml")
            }
        } catch (e: Exception) {
            Log.e("LocationPicker", "Failed to initialize Places API", e)
        }
    }

    val placesClient = remember {
        if (Places.isInitialized()) {
            Places.createClient(context)
        } else {
            null
        }
    }

    val token = remember { AutocompleteSessionToken.newInstance() }

    // Search predictions with debounce
    LaunchedEffect(searchQuery) {
        searchJob?.cancel()
        if (searchQuery.length >= 3 && placesClient != null) {
            searchJob = scope.launch {
                delay(500) // Debounce for 500ms
                val request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setQuery(searchQuery)
                    .build()

                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        predictions = response.autocompletePredictions
                        showPredictions = true
                    }
                    .addOnFailureListener { exception ->
                        Log.e("LocationPicker", "Place prediction failed", exception)
                    }
            }
        } else {
            predictions = emptyList()
            showPredictions = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Location",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Search box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Search for a location") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Predictions dropdown
                if (showPredictions && predictions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .heightIn(max = 200.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        LazyColumn {
                            items(predictions) { prediction ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            placesClient?.let { client ->
                                                // Fetch place details
                                                val placeFields = listOf(
                                                    Place.Field.LAT_LNG,
                                                    Place.Field.NAME
                                                )
                                                val request = FetchPlaceRequest
                                                    .builder(prediction.placeId, placeFields)
                                                    .build()

                                                client
                                                    .fetchPlace(request)
                                                    .addOnSuccessListener { response ->
                                                        val place = response.place
                                                        place.latLng?.let { latLng ->
                                                            selectedLatLng = latLng
                                                            markerPosition = latLng
                                                            showPredictions = false

                                                            // Get formatted address
                                                            scope.launch {
                                                                val address = getAddressFromLocation(context, latLng)
                                                                if (address != null) {
                                                                    searchQuery = address
                                                                } else {
                                                                    // Fallback to prediction text
                                                                    searchQuery = prediction.getPrimaryText(null).toString()
                                                                }

                                                                // Animate camera to new position
                                                                cameraPositionState.animate(
                                                                    update = CameraUpdateFactory
                                                                        .newLatLngZoom(latLng, 15f),
                                                                    durationMs = 1000
                                                                )
                                                            }
                                                        }
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        Log.e(
                                                            "LocationPicker",
                                                            "Place fetch failed",
                                                            exception
                                                        )
                                                    }
                                            }
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = prediction.getPrimaryText(null).toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = prediction.getSecondaryText(null).toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (prediction != predictions.last()) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.2f
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Google Map
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            markerPosition = latLng
                            selectedLatLng = latLng
                            // Perform reverse geocoding
                            scope.launch {
                                val address = getAddressFromLocation(context, latLng)
                                if (address != null) {
                                    searchQuery = address
                                }
                            }
                        }
                    ) {
                        val markerState = rememberMarkerState(position = markerPosition)

                        // Update marker position when markerPosition changes
                        LaunchedEffect(markerPosition) {
                            markerState.position = markerPosition
                        }

                        Marker(
                            state = markerState,
                            title = "Event Location",
                            draggable = true
                        )

                        // Listen to marker drag and perform reverse geocoding
                        LaunchedEffect(markerState.position) {
                            if (markerState.position != markerPosition) {
                                markerPosition = markerState.position
                                selectedLatLng = markerState.position

                                // Perform reverse geocoding when marker is dragged
                                val address = getAddressFromLocation(context, markerState.position)
                                if (address != null) {
                                    searchQuery = address
                                }
                            }
                        }
                    }
                }

                // Confirm button
                Button(
                    onClick = {
                        onLocationSelected(searchQuery, selectedLatLng)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Confirm Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
