package no.ntnu.prog2007.ihost.ui.screens.events.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.data.model.Event

@Composable
fun EventItem(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (event.description != null) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Dato: ${event.eventDate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (event.eventTime != null) {
                        Text(
                            text = "Tid: ${event.eventTime}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (event.location != null) {
                        Text(
                            text = "Sted: ${event.location}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text(
                    text = "${event.attendees.size} deltakere",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }
        }
    }
}
