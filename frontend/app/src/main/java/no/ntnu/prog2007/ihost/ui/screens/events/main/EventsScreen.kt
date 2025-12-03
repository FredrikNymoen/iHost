package no.ntnu.prog2007.ihost.ui.screens.events.main

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.ntnu.prog2007.ihost.ui.screens.events.main.components.JoinEventDialog
import no.ntnu.prog2007.ihost.ui.screens.events.main.components.EventItem
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: EventViewModel,
    authViewModel: AuthViewModel,
    onEventClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val currentUserId = authUiState.currentUser?.uid
    val context = LocalContext.current

    // Dialog state
    var showJoinDialog by remember { mutableStateOf(false) }
    var isJoining by remember { mutableStateOf(false) }

    // Tab state (0 = Invites, 1 = My events)
    var selectedTab by remember { mutableStateOf(0) }

    // Time filter state (0 = Future, 1 = Past)
    var timeFilter by remember { mutableStateOf(0) }

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
                modifier = Modifier.offset(y = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton( // Button to join event with code
                    onClick = { showJoinDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
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
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
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
            modifier = Modifier.fillMaxSize()
                // Skip all scaffold padding to avoid colored boxes
        ) {
            // Top section: Tabs and filters with minimal padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Tab buttons: Invites vs My events
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Invites button
                    Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                            contentColor = if (selectedTab == 0) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (selectedTab == 0) 6.dp else 2.dp
                        )
                    ) {
                        Text(
                            "Invites",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    // My events button
                    Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                            contentColor = if (selectedTab == 1) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (selectedTab == 1) 6.dp else 2.dp
                        )
                    ) {
                        Text(
                            "My events",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date and time filter row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current date
                    Text(
                        text = LocalDate.now().format(
                            DateTimeFormatter.ofPattern("dd.MM.yy")
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Future/Past toggle with segmented control style
                    Surface(
                        modifier = Modifier.height(36.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Future button
                            Surface(
                                onClick = { timeFilter = 0 },
                                modifier = Modifier.height(28.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                color = if (timeFilter == 0) MaterialTheme.colorScheme.primary else Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Future",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (timeFilter == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }

                            // Past button
                            Surface(
                                onClick = { timeFilter = 1 },
                                modifier = Modifier.height(28.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                color = if (timeFilter == 1) MaterialTheme.colorScheme.primary else Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Past",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (timeFilter == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }


            // Events list
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                uiState.errorMessage != null -> {
                    ErrorMessage(
                        message = uiState.errorMessage!!,
                        onDismiss = { viewModel.clearError() }
                    )
                }
                else -> {
                    // Filter events based on selected tab and time filter
                    val filteredEvents = uiState.events.filter { eventWithMetadata ->
                        val isMyEvent = eventWithMetadata.userRole == "CREATOR"
                        val tabMatch = when (selectedTab) {
                            0 -> !isMyEvent // Invites: not creator
                            1 -> isMyEvent  // My events: is creator
                            else -> true
                        }

                        // Time filter
                        val currentDate = LocalDate.now()
                        val eventDate = try {
                            LocalDate.parse(eventWithMetadata.event.eventDate)
                        } catch (e: Exception) {
                            currentDate // Default to current date if parsing fails
                        }

                        val timeMatch = when (timeFilter) {
                            0 -> !eventDate.isBefore(currentDate) // Future: today or later
                            1 -> eventDate.isBefore(currentDate)  // Past: before today
                            else -> true
                        }

                        tabMatch && timeMatch
                    }

                    if (filteredEvents.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No events found",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        val sortedEvents = filteredEvents.sortedWith(
                            compareBy({ it.event.eventDate }, { it.event.eventTime ?: "" })
                        )
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                Text("Close")
            }
        }
    ) {
        Text(message)
    }
}
