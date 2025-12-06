package no.ntnu.prog2007.ihost.ui.screens.events.editevent.components

import androidx.compose.runtime.Composable
import no.ntnu.prog2007.ihost.ui.components.events.EventDetailsForm

/**
 * Edit event details form component
 *
 * Wrapper around EventDetailsForm configured for event editing.
 * Disables location error display and adjusts padding for use within EditEventScreen.
 *
 * @param title Current event title
 * @param description Current event description
 * @param eventDate Current event date (ISO format string)
 * @param eventTime Current event time (HH:mm format)
 * @param location Current event location
 * @param isLoading Whether form fields should be disabled during submission
 * @param onTitleChange Callback invoked when title changes
 * @param onDescriptionChange Callback invoked when description changes
 * @param onDateClick Callback to show date picker dialog
 * @param onTimeClick Callback to show time picker dialog
 * @param onLocationClick Callback to show location picker dialog
 */
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
