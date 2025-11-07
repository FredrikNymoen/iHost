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
import androidx.compose.material.icons.filled.Person
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

import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import java.time.temporal.ChronoUnit


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
            TopAppBar(
                title = {
                    Text(
                        event?.title ?: "Event Details",
                        color = Color(0xFFFFC107),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Medium
                    )
                }, navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFC107)
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
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Localized description"
                        )
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
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

                Row {
                    //left column with date and time
                    Column(
                        modifier = Modifier.weight(0.5f), horizontalAlignment = Alignment.Start
                    ) {
                        //Date field
                        Row(
                            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Date",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(36.dp),
                            )
                            Text(
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .padding(start = 24.dp)
                                    .fillMaxHeight()
                                    .align(alignment = Alignment.CenterVertically),
                                text = event.eventDate,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White

                            )
                        }
                        //Time field
                        if (!event.eventTime.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Time",
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .padding(start = 24.dp)
                                        .fillMaxHeight()
                                        .align(alignment = Alignment.CenterVertically),
                                    text = event.eventTime,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    // Right column with location and price

                    Column(
                        modifier = Modifier.weight(0.5f), horizontalAlignment = Alignment.End
                    ) {
                        if (!event.location.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
                                horizontalArrangement = Arrangement.End  // This aligns content to the right

                            ) {

                                Text(
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .weight(1f),  // Takes remaining space
                                    text = event.location,
                                    color = Color.White,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                    maxLines = 2,  // Allow 2 lines
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Location",
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable {  // Make the icon clickable
                                            // Open Google Maps with the location
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse(
                                                    "geo:0,0?q=${
                                                        android.net.Uri.encode(
                                                            event.location
                                                        )
                                                    }"
                                                )
                                            )
                                            intent.setPackage("com.google.android.apps.maps")  // Force Google Maps

                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                // If Google Maps not installed, use browser
                                                val webIntent = android.content.Intent(
                                                    android.content.Intent.ACTION_VIEW,
                                                    android.net.Uri.parse(
                                                        "https://www.google.com/maps/search/?api=1&query=${
                                                            android.net.Uri.encode(
                                                                event.location
                                                            )
                                                        }"
                                                    )
                                                )
                                                context.startActivity(webIntent)
                                            }
                                        },
                                )

                            }
                        }
                        Row(
                            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Attendees",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .fillMaxHeight()
                                    .align(alignment = Alignment.CenterVertically),
                                text = "${confirmedAttendees.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }


                    }
                }


                Spacer(modifier = Modifier.height(16.dp))



                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF0C5CA7), shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Host",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${attendeeFirsNames[event.creatorUid]} ${attendeeLastNames[event.creatorUid]} (${attendeeUserNames[event.creatorUid]})" ?: "Loading...",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }

                    if (confirmedAttendees.isEmpty()) {
                        Text(
                            text = "No attendees yet",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = Color(0xFFB0B0B0)
                        )
                    } else {
                        confirmedAttendees.filter { eventUser ->
                            !eventUser.userId.equals(event.creatorUid, true)
                        }.filter { eventUser -> eventUser.status.equals("ACCEPTED") }
                            .forEach { eventUser ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Attending",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${attendeeFirsNames[eventUser.userId]} ${attendeeLastNames[eventUser.userId]}(${attendeeUserNames[eventUser.userId]})" ?: "Loading...",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Show payment error if any
                /*
                stripeUiState.paymentError?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Text(
                            text = error,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }*/

                // Action Buttons
                val isCreator = event.creatorUid == currentUserId
                val userStatus = eventWithMetadata?.userStatus
                val isAccepted = userStatus == "ACCEPTED" || userStatus == "CREATOR"
                val isPending = userStatus == "PENDING"
                val isDeclined = userStatus == "DECLINED"

                if (isCreator) {

                    // Invite Users Button
                    Button(
                        onClick = { onInviteUsers(eventId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(bottom = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0C5CA7)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Invite Users",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Invite Users")
                    }

                    // Edit and Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onEdit(eventId) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00BCD4)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit")
                        }

                        Button(
                            onClick = {
                                viewModel.deleteEvent(eventId)
                                onBack()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete")
                        }
                    }
                } else if (isAccepted) {
                    // Already accepted - Show status
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Accepted",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "You're attending this event",
                                color = Color(0xFF4CAF50),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (isPending) {
                    // Pending invitation - Show Accept/Decline buttons
                    Text(
                        text = "You're invited!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val myEventUser = eventAttendees.find { it.userId == currentUserId }
                                if (myEventUser != null) {
                                    // ACTIVATE THIS IF TEST then payment system implemented

                                    /*if (!event.free && activity != null) {
                                        // Paid event - initiate payment first
                                        stripeViewModel.initiatePayment(
                                            activity = activity,
                                            eventId = eventId,
                                            onPaymentComplete = {
                                                // Payment succeeded, now accept invitation
                                                viewModel.acceptInvitation(
                                                    eventUserId = myEventUser.id,
                                                    onSuccess = {
                                                        Toast.makeText(
                                                            context,
                                                            "Payment successful! Invitation accepted!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        stripeViewModel.clearPaymentSuccess()
                                                    },
                                                    onError = { error ->
                                                        Toast.makeText(
                                                            context,
                                                            "Payment succeeded but acceptance failed: $error",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    })
                                            })
                                    }*/

                                    // Free event - just accept
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
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            enabled = !stripeUiState.isProcessingPayment
                        ) {
                            if (stripeUiState.isProcessingPayment) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Accept",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (!event.free) "Buy & Accept" else "Accept")
                            }
                        }

                        Button(
                            onClick = {
                                // Find the eventUser document for this user
                                val myEventUser = eventAttendees.find { it.userId == currentUserId }
                                if (myEventUser != null) {
                                    viewModel.declineInvitation(
                                        eventUserId = myEventUser.id,
                                        onSuccess = {
                                            Toast.makeText(
                                                context, "Invitation declined", Toast.LENGTH_SHORT
                                            ).show()
                                            onBack() // Go back to events list
                                        },
                                        onError = { error ->
                                            Toast.makeText(
                                                context, "Error: $error", Toast.LENGTH_SHORT
                                            ).show()
                                        })
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Text("Decline")
                        }
                    }
                } else if (isDeclined) {
                    // Declined invitation
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFD32F2F).copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "You have declined this event",
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Not invited - Cannot access
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFD32F2F).copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "You are not invited to this event",
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFC107))
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
            .wrapContentHeight(),  // Expands with content
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(  // Use Column to stack image and description
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Image Section with Timer overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFF003D73))  // ADD THIS - same color as description
            ) {
                val firstImageUrl = eventImages?.firstOrNull()?.path

                if (firstImageUrl != null) {
                    AsyncImage(
                        model = firstImageUrl,
                        contentDescription = "Event image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    // Show gradient background when no image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF0C5CA7),
                                        Color(0xFF003D73)
                                    )
                                )
                            )
                    )
                }

                // Timer overlay at TOP of image with background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)  // Position at top
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(
                                color = Color.Black.copy(alpha = 0.2f),  // 20% visibility black background
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        EventTimer(event.eventDate, event.eventTime)
                    }
                }
            }

            // Description below image - Expands with text
            if (event.description != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()  // Expands downward with text
                        .background(Color(0xFF003D73))
                        .padding(16.dp)
                ) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
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
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)

    )
}