package no.ntnu.prog2007.ihost.ui.screens.events.eventdetail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.ui.components.TopBar
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.StripeViewModel
import no.ntnu.prog2007.ihost.ui.screens.events.eventdetail.components.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    viewModel: EventViewModel,
    authViewModel: AuthViewModel,
    stripeViewModel: StripeViewModel,
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
            val user = viewModel.getUserByUid(eventUser.userId)
            if (user != null) {
                userNames[eventUser.userId] = user.username
                firstNames[eventUser.userId] = user.firstName
                lastNames[eventUser.userId] = user.lastName ?: ""
            }
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
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                "geo:0,0?q=${Uri.encode(event.location)}"
                            )
                        )
                        intent.setPackage("com.google.android.apps.maps")

                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // If Google Maps not installed, use browser
                            val webIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://www.google.com/maps/search/?api=1&query=${
                                        Uri.encode(event.location)
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
