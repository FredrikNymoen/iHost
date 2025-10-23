package no.ntnu.prog2007.ihost.ui.screens.events.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.ntnu.prog2007.ihost.data.model.Event
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.ui.theme.MediumBlue
import no.ntnu.prog2007.ihost.ui.theme.Gold

@Composable
fun EventItem(
    event: Event,
    authViewModel: AuthViewModel,
    onClick: () -> Unit
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val currentUserId = authUiState.currentUser?.uid
    val isAttending = currentUserId != null && event.attendees.contains(currentUserId)
    val isPending = currentUserId != null && !event.attendees.contains(currentUserId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MediumBlue)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side - Event details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Date and time
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = event.eventDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        if (event.eventTime != null) {
                            Text(
                                text = event.eventTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Status badge
                    if (isAttending) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Accepted",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "accepted",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                                fontSize = 10.sp
                            )
                        }
                    } else if (isPending) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFFFC107).copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WatchLater,
                                contentDescription = "Pending",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "pending",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFC107),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Title
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Description
                    if (event.description != null) {
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp
                        )
                    }
                }

                // Right side - Attendees count
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "${event.attendees.size}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "deltakere",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
