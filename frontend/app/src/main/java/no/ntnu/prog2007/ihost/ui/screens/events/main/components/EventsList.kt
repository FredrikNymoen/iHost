package no.ntnu.prog2007.ihost.ui.screens.events.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.data.model.domain.EventWithMetadata
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import java.time.LocalDate

/**
 * Events list component
 *
 * Displays filtered and paginated list of events.
 * Filters by tab (invites vs my events) and time (future vs past).
 * Shows event cards that navigate to detail view on click.
 *
 * @param events List of all events to filter and display
 * @param selectedTab 0=invites (pending), 1=my events (created or accepted)
 * @param timeFilter 0=future events, 1=past events
 * @param authViewModel For current user context
 * @param viewModel For event operations
 * @param onEventClick Callback invoked when event card is clicked with event ID
 * @param modifier Optional Modifier for customizing layout
 */
@Composable
fun EventsList(
    events: List<EventWithMetadata>,
    selectedTab: Int,
    timeFilter: Int,
    authViewModel: AuthViewModel,
    viewModel: EventViewModel,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter events based on selected tab and time filter
    val filteredEvents = events.filter { eventWithMetadata ->
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
            modifier = modifier.fillMaxSize(),
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
            modifier = modifier,
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
