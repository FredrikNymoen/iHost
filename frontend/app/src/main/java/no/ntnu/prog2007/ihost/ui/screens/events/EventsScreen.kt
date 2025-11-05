package no.ntnu.prog2007.ihost.ui.screens.events

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.ui.components.JoinEventDialog
import no.ntnu.prog2007.ihost.ui.screens.events.components.EventItem
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: EventViewModel,
    authViewModel: AuthViewModel,
    onEventClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Dialog state
    var showJoinDialog by remember { mutableStateOf(false) }
    var isJoining by remember { mutableStateOf(false) }

    // Load events when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.ensureEventsLoaded()
    }

    // Show join dialog
    if (showJoinDialog) {
        JoinEventDialog(
            onDismiss = { showJoinDialog = false },
            onSubmit = { code ->
                isJoining = true
                viewModel.getEventByCode(code) { eventWithMetadata ->
                    isJoining = false
                    showJoinDialog = false
                    onEventClick(eventWithMetadata.id)

                    Toast.makeText(
                        context,
                        "Event '${eventWithMetadata.event.title}' found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            isLoading = isJoining
        )
    }

    // Main screen layout
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = { // Setting floating action buttons to a column
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton( // Button to join event with code
                    onClick = { showJoinDialog = true },
                    containerColor = Color(0xFFFFC107), // Gold color
                    contentColor = Color(0xFF001D3D), // Dark blue text
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Join Event with Code"
                    )
                }

                FloatingActionButton( // Button to refresh events
                    onClick = { viewModel.loadEvents() },
                    containerColor = Color(0xFF0C5CA7), // Medium blue
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                uiState.errorMessage != null -> {
                    ErrorMessage(
                        message = uiState.errorMessage!!,
                        onDismiss = { viewModel.clearError() }
                    )
                }
                uiState.events.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ingen events funnet")
                    }
                }
                else -> {
                    val sortedEvents = uiState.events.sortedWith(
                        compareBy({ it.event.eventDate }, { it.event.eventTime ?: "" })
                    )
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sortedEvents) { eventWithMetadata ->
                            EventItem(
                                eventWithMetadata = eventWithMetadata,
                                authViewModel = authViewModel,
                                viewModel = viewModel,
                                onClick = { onEventClick(eventWithMetadata.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
            TextButton(onClick = onDismiss) {
                Text("Lukk")
            }
        }
    ) {
        Text(message)
    }
}
