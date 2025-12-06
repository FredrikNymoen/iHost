package no.ntnu.prog2007.ihost.ui.screens.events.eventdetail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Info card component
 *
 * Modern card displaying event information with icon, label, and value.
 * Optionally clickable for user interactions like opening maps for location.
 *
 * @param icon Icon to display in the card
 * @param label Label describing the information
 * @param value The actual value or content to display
 * @param onClick Optional click handler for making card interactive
 * @param modifier Optional Modifier for customizing layout
 */
@Composable
fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Column(
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Event Info Section with Grid Layout
@Composable
fun EventInfoSection(
    eventDate: String,
    eventTime: String?,
    location: String?,
    attendeeCount: Int,
    onLocationClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                icon = Icons.Default.CalendarMonth,
                label = "Date",
                value = eventDate,
                modifier = Modifier.weight(1f)
            )
            if (!eventTime.isNullOrBlank()) {
                InfoCard(
                    icon = Icons.Default.AccessTime,
                    label = "Time",
                    value = eventTime,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!location.isNullOrBlank()) {
                InfoCard(
                    icon = Icons.Default.Map,
                    label = "Location",
                    value = location,
                    onClick = onLocationClick,
                    modifier = Modifier.weight(1f)
                )
            }
            InfoCard(
                icon = Icons.Default.People,
                label = "Attendees",
                value = attendeeCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
