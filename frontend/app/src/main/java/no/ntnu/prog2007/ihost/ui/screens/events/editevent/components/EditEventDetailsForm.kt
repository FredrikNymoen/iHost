package no.ntnu.prog2007.ihost.ui.screens.events.editevent.components

import androidx.compose.runtime.Composable
import no.ntnu.prog2007.ihost.ui.components.event.EventDetailsForm

@Composable
fun EditEventDetailsForm(
    title: String,
    description: String,
    eventDate: String,
    eventTime: String,
    location: String,
    isLoading: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onLocationClick: () -> Unit
) {
    EventDetailsForm(
        title = title,
        onTitleChange = onTitleChange,
        description = description,
        onDescriptionChange = onDescriptionChange,
        eventDate = eventDate,
        onDateClick = onDateClick,
        eventTime = eventTime,
        onTimeClick = onTimeClick,
        location = location,
        onLocationClick = onLocationClick,
        isLoading = isLoading,
        showLocationError = false,
        topPadding = 32
    )
}
