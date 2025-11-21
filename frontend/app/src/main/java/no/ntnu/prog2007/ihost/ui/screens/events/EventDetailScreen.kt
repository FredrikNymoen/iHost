package no.ntnu.prog2007.ihost.ui.screens.events

import android.content.Intent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.ntnu.prog2007.ihost.data.model.Event
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.FlowRow
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import java.time.temporal.ChronoUnit
import no.ntnu.prog2007.ihost.ui.components.UserCard
import no.ntnu.prog2007.ihost.ui.components.TopBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    viewModel: EventViewModel,
    authViewModel: AuthViewModel,
    stripeViewModel: no.ntnu.prog2007.ihost.viewmodel.StripeViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onInviteUsers: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val eventWithMetadata = uiState.events.find { it.id == eventId }
    val event = eventWithMetadata?.event
    val currentUserId = authUiState.currentUser?.uid
    val context = LocalContext.current


    // Get the ComponentActivity from context
    val activity = context as? ComponentActivity

    // State to hold attendee names mapping
    var attendeeUserNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var attendeeFirsNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var attendeeLastNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Stripe state
    val stripeUiState by stripeViewModel.uiState.collectAsState()

    // Get attendees from event_users
    val eventAttendees = uiState.eventAttendees[eventId] ?: emptyList()

    // Attendees Section
    val confirmedAttendees = eventAttendees.filter {
        it.status == "ACCEPTED" || it.status == "CREATOR"
    }

    // Load event images and attendees when the screen loads
    LaunchedEffect(eventId) {
        viewModel.loadEventImages(eventId)
        viewModel.loadEventAttendees(eventId)
    }

    // Fetch attendee names
    LaunchedEffect(eventAttendees) {
        val userNames = mutableMapOf<String, String>()
        val firstNames = mutableMapOf<String, String>()
        val lastNames = mutableMapOf<String, String>()

        for (eventUser in eventAttendees) {
            val userName = viewModel.getUserUserName(eventUser.userId)
            val firstName = viewModel.getUserFirstName(eventUser.userId)
            val lastName = viewModel.getUserLastName(eventUser.userId)
            userNames[eventUser.userId] = userName
            firstNames[eventUser.userId] = firstName
            lastNames[eventUser.userId] = if (lastName != null) lastName else ""
        }
        attendeeUserNames = userNames
        attendeeFirsNames = firstNames
        attendeeLastNames = lastNames
    }

    Scaffold(
        topBar = {
            //Toppbar for event description screen
            TopBar(
                title = {
                    Text(event?.title ?: "Event Details")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                // Icon Button to share your event with others
                actions = {
                    IconButton(onClick = {
                        val shareMessage =
                            "Join my event '${event?.title}' on iHost! Use the share code: ${event?.shareCode}"
                        // Copy button
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                        }
                        context.startActivity(
                            Intent.createChooser(
                                intent, "Share event code"
                            )
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        }, containerColor = Color.Transparent
    ) { padding ->
        if (event != null && currentUserId != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Event Card Header
                EventDetailHeader(event, uiState.eventImages[eventId])

                Spacer(modifier = Modifier.height(24.dp))

                // Event Info Section - Modern Card Grid
                EventInfoSection(
                    eventDate = event.eventDate,
                    eventTime = event.eventTime,
                    location = event.location,
                    attendeeCount = confirmedAttendees.size,
                    onLocationClick = {
                        // Open Google Maps with the location
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(
                                "geo:0,0?q=${android.net.Uri.encode(event.location)}"
                            )
                        )
                        intent.setPackage("com.google.android.apps.maps")

                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // If Google Maps not installed, use browser
                            val webIntent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(
                                    "https://www.google.com/maps/search/?api=1&query=${
                                        android.net.Uri.encode(event.location)
                                    }"
                                )
                            )
                            context.startActivity(webIntent)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Attendees Section - Modern Card with Chips
                AttendeesSection(
                    hostName = "${attendeeFirsNames[event.creatorUid]} ${attendeeLastNames[event.creatorUid]}",
                    hostUsername = attendeeUserNames[event.creatorUid] ?: "Loading...",
                    attendees = confirmedAttendees.filter { eventUser ->
                        !eventUser.userId.equals(event.creatorUid, true) && eventUser.status == "ACCEPTED"
                    }.map { eventUser ->
                        AttendeeInfo(
                            name = "${attendeeFirsNames[eventUser.userId]} ${attendeeLastNames[eventUser.userId]}",
                            username = attendeeUserNames[eventUser.userId] ?: "Loading..."
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                val isCreator = event.creatorUid == currentUserId
                val userStatus = eventWithMetadata?.userStatus
                val isAccepted = userStatus == "ACCEPTED" || userStatus == "CREATOR"
                val isPending = userStatus == "PENDING"
                val isDeclined = userStatus == "DECLINED"

                if (isCreator) {
                    // Creator Action Buttons
                    CreatorActionButtons(
                        onInviteUsers = { onInviteUsers(eventId) },
                        onEdit = { onEdit(eventId) },
                        onDelete = {
                            viewModel.deleteEvent(eventId)
                            onBack()
                        }
                    )
                } else if (isAccepted) {
                    // Accepted Status Card
                    StatusCard(
                        icon = Icons.Default.Check,
                        message = "You're attending this event",
                        statusType = StatusType.ACCEPTED
                    )
                } else if (isPending) {
                    // Pending Invitation - Accept/Decline Buttons
                    PendingInvitationActions(
                        isFreeEvent = event.free,
                        isProcessingPayment = stripeUiState.isProcessingPayment,
                        onAccept = {
                            val myEventUser = eventAttendees.find { it.userId == currentUserId }
                            if (myEventUser != null) {
                                viewModel.acceptInvitation(
                                    eventUserId = myEventUser.id,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "Invitation accepted!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(
                                            context, "Error: $error", Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            }
                        },
                        onDecline = {
                            val myEventUser = eventAttendees.find { it.userId == currentUserId }
                            if (myEventUser != null) {
                                viewModel.declineInvitation(
                                    eventUserId = myEventUser.id,
                                    onSuccess = {
                                        Toast.makeText(
                                            context, "Invitation declined", Toast.LENGTH_SHORT
                                        ).show()
                                        onBack()
                                    },
                                    onError = { error ->
                                        Toast.makeText(
                                            context, "Error: $error", Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            }
                        }
                    )
                } else if (isDeclined) {
                    // Declined Status Card
                    StatusCard(
                        message = "You have declined this event",
                        statusType = StatusType.DECLINED
                    )
                } else {
                    // Not Invited Status Card
                    StatusCard(
                        message = "You are not invited to this event",
                        statusType = StatusType.NOT_INVITED
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun EventDetailHeader(
    event: Event,
    eventImages: List<no.ntnu.prog2007.ihost.data.remote.EventImage>?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                // Image Section with Timer and dark overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    val firstImageUrl = eventImages?.firstOrNull()?.path

                    // Background image or gradient
                    if (firstImageUrl != null) {
                        AsyncImage(
                            model = firstImageUrl,
                            contentDescription = "Event image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        // Show gradient background when no image
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                        )
                    }

                    // Dark overlay on entire image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )

                    // Timer centered on image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EventTimer(event.eventDate, event.eventTime)
                    }
                }

                // Description below image
                if (event.description != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventTimer(eventDate: String?, eventTime: String?) {
    var timeRemaining by remember { mutableStateOf("") }
    var checkedTime = "00:00"
    if (!eventTime.isNullOrBlank()) {
        checkedTime = eventTime

    }
    LaunchedEffect(eventDate, checkedTime) {
        while (true) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val targetDateTime = LocalDateTime.parse("$eventDate $checkedTime", formatter)
            val currentDateTime = LocalDateTime.now()

            val diffMillis = ChronoUnit.MILLIS.between(currentDateTime, targetDateTime)

            timeRemaining = if (diffMillis > 0) {
                val days = diffMillis / (1000 * 60 * 60 * 24)
                val hours = (diffMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                val minutes = (diffMillis % (1000 * 60 * 60)) / (1000 * 60)
                val seconds = (diffMillis % (1000 * 60)) / 1000
                if (days > 2) {
                    String.format("%02d days", days)
                } else if (hours < 12 && days < 1) {
                    String.format("%02d hours %02d minutes %02d seconds", hours, minutes, seconds)
                } else {
                    String.format("%02d day(s) %02d hours", days, hours)
                }
            } else {
                "Event started"
            }

            delay(1000) // Update every second
        }
    }

    Text(
        text = timeRemaining,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}

// ==================== NEW MODERN COMPOSABLES ====================

// Data class for attendee info
data class AttendeeInfo(
    val name: String,
    val username: String
)

// Enum for status types
enum class StatusType {
    ACCEPTED, PENDING, DECLINED, NOT_INVITED
}

// Modern Info Card Component
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
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
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

// Host Chip Component
@Composable
fun HostChip(
    name: String,
    username: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFC107).copy(alpha = 0.09f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Host",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// Attendee Chip Component
@Composable
fun AttendeeChip(
    name: String,
    username: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Attending",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// Attendees Section
@Composable
fun AttendeesSection(
    hostName: String,
    hostUsername: String,
    attendees: List<AttendeeInfo>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Attendees",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            // Host chip
            HostChip(name = hostName, username = hostUsername)

            // Attendee chips
            if (attendees.isEmpty()) {
                Text(
                    text = "No other attendees yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                attendees.forEach { attendee ->
                    AttendeeChip(name = attendee.name, username = attendee.username)
                }
            }
        }
    }
}

// Creator Action Buttons
@Composable
fun CreatorActionButtons(
    onInviteUsers: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Invite Users Button
        FilledTonalButton(
            onClick = onInviteUsers,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Invite Users",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Invite Users",
                style = MaterialTheme.typography.titleSmall
            )
        }

        // Edit and Delete Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Button(
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

// Pending Invitation Actions
@Composable
fun PendingInvitationActions(
    isFreeEvent: Boolean,
    isProcessingPayment: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "You're invited!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAccept,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isProcessingPayment,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessingPayment) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFreeEvent) "Accept" else "Buy & Accept",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Button(
                onClick = onDecline,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Decline",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

// Status Card Component
@Composable
fun StatusCard(
    message: String,
    statusType: StatusType,
    icon: ImageVector? = null
) {
    val (backgroundColor, textColor, iconColor) = when (statusType) {
        StatusType.ACCEPTED -> Triple(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.onSecondary,
            MaterialTheme.colorScheme.onSecondary
        )
        StatusType.PENDING -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            MaterialTheme.colorScheme.tertiary
        )
        StatusType.DECLINED, StatusType.NOT_INVITED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.error
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = message,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = message,
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}