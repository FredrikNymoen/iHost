package no.ntnu.prog2007.ihost.ui.components.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag

/**
 * Event details input form component
 *
 * Provides a comprehensive form for creating or editing event details including
 * title, description, date, time, and location. Date, time, and location fields
 * are clickable and trigger respective picker dialogs.
 *
 * @param title Current event title value
 * @param onTitleChange Callback invoked when title text changes
 * @param description Current event description value
 * @param onDescriptionChange Callback invoked when description text changes
 * @param eventDate Formatted event date string (read-only, clickable)
 * @param onDateClick Callback invoked when date field is clicked
 * @param eventTime Formatted event time string (read-only, clickable)
 * @param onTimeClick Callback invoked when time field is clicked
 * @param location Current location string (read-only, clickable)
 * @param onLocationClick Callback invoked when location field is clicked
 * @param isLoading Whether the form is in loading state (disables editable fields)
 * @param showLocationError Whether to show error state on location field
 * @param topPadding Top padding in dp for the form
 */
@Composable
fun EventDetailsForm(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    eventDate: String,
    onDateClick: () -> Unit,
    eventTime: String,
    onTimeClick: () -> Unit,
    location: String,
    onLocationClick: () -> Unit,
    isLoading: Boolean,
    showLocationError: Boolean = false,
    topPadding: Int = 18
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Event Title", color = MaterialTheme.colorScheme.onSurface) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topPadding.dp)
                .padding(bottom = 16.dp)
                .testTag("eventTitleField"),
            enabled = !isLoading,
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (optional)", color = MaterialTheme.colorScheme.onSurface) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .heightIn(min = 80.dp)
                .testTag("eventDescriptionField"),
            minLines = 3,
            enabled = !isLoading,
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        OutlinedTextField(
            value = eventDate,
            onValueChange = { },
            label = { Text("Date", color = MaterialTheme.colorScheme.onSurface) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { onDateClick() }
                .testTag("eventDateField"),
            enabled = false,
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select date",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.primary,
                disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = MaterialTheme.colorScheme.primary
            )
        )

        OutlinedTextField(
            value = eventTime,
            onValueChange = { },
            label = { Text("Time (optional)", color = MaterialTheme.colorScheme.onSurface) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { onTimeClick() }
                .testTag("eventTimeField"),
            enabled = false,
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Select time",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.primary,
                disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = MaterialTheme.colorScheme.primary
            )
        )

        OutlinedTextField(
            value = location,
            onValueChange = { },
            label = { Text("Location (optional)", color = MaterialTheme.colorScheme.onSurface) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { onLocationClick() }
                .testTag("eventLocationField"),
            enabled = false,
            readOnly = true,
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.primary,
                disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = MaterialTheme.colorScheme.primary
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Select location",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            isError = showLocationError && location.isEmpty()
        )
    }
}
