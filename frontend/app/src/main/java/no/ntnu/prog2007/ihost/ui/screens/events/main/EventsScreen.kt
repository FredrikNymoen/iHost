package no.ntnu.prog2007.ihost.ui.screens.events.main

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.ui.screens.events.main.components.JoinEventDialog
import no.ntnu.prog2007.ihost.ui.screens.events.main.components.FloatingActionButtons
import no.ntnu.prog2007.ihost.ui.screens.events.main.components.TabSelector
import no.ntnu.prog2007.ihost.ui.screens.events.main.components.DateFilterBar
import no.ntnu.prog2007.ihost.ui.screens.events.main.components.EventsList
import no.ntnu.prog2007.ihost.ui.screens.events.main.components.ErrorMessage
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
        floatingActionButton = {
            FloatingActionButtons(
                onJoinEvent = { showJoinDialog = true },
                onRefresh = { viewModel.loadEvents() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top section: Tabs and filters with minimal padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Tab buttons: Invites vs My events
                TabSelector(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date and time filter row
                DateFilterBar(
                    timeFilter = timeFilter,
                    onTimeFilterChange = { timeFilter = it }
                )
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
                    EventsList(
                        events = uiState.events,
                        selectedTab = selectedTab,
                        timeFilter = timeFilter,
                        authViewModel = authViewModel,
                        viewModel = viewModel,
                        onEventClick = onEventClick
                    )
                }
            }
        }
    }
}
