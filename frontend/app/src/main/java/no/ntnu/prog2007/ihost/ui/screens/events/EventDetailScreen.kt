package no.ntnu.prog2007.ihost.ui.screens.events

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.ntnu.prog2007.ihost.data.model.Event
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.text.style.TextAlign

import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAmount


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    viewModel: EventViewModel,
    authViewModel: AuthViewModel,
    stripeViewModel: no.ntnu.prog2007.ihost.viewmodel.StripeViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val event = uiState.events.find { it.id == eventId }
    val currentUserId = authUiState.currentUser?.uid
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Get the ComponentActivity from context
    val activity = context as? ComponentActivity

    // State to hold attendee names mapping
    var attendeeNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Stripe state
    val stripeUiState by stripeViewModel.uiState.collectAsState()

    // Fetch attendee names
    LaunchedEffect(event?.attendees) {
        if (event != null) {
            val names = mutableMapOf<String, String>()
            for (attendeeId in event.attendees) {
                try {
                    val user = RetrofitClient.apiService.getUserByUid(attendeeId)
                    names[attendeeId] = user.displayName
                } catch (e: Exception) {
                    names[attendeeId] = "User" // Fallback if fetch fails
                }
            }
            attendeeNames = names
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        event?.title ?: "Event Details",
                        color = Color(0xFFFFC107),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFC107)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
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
                EventDetailHeader(event)

                Spacer(modifier = Modifier.height(24.dp))

                // Date and Time Section
                SectionTitle("Date & Time")
                EventDetailItem(
                    label = "Date",
                    value = event.eventDate
                )
                if (event.eventTime != null) {
                    EventDetailItem(
                        label = "Time",
                        value = event.eventTime
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Location Section
                if (event.location != null) {
                    SectionTitle("Location")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF0C5CA7),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Price Section
                if (!event.free) {
                    SectionTitle("Price")
                    EventDetailItem(
                        label = "Cost",
                        value = "${String.format("%.2f", event.price)} NOK"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    SectionTitle("Price")
                    EventDetailItem(
                        label = "Cost",
                        value = "Free"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Description Section
                if (event.description != null) {
                    SectionTitle("Description")
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF0C5CA7),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Creator Info
                SectionTitle("Host")
                EventDetailItem(
                    label = "Name",
                    value = event.creatorName ?: "Anonymous"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Attendees Section
                SectionTitle("Attendees (${event.attendees.size})")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF0C5CA7),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (event.attendees.isEmpty()) {
                        Text(
                            text = "No attendees yet",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = Color(0xFFB0B0B0)
                        )
                    } else {
                        event.attendees.forEach { attendeeId ->
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
                                    text = attendeeNames[attendeeId] ?: "Loading...",
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
                }

                // Action Buttons
                val isCreator = event.creatorUid == currentUserId
                val isAttending = event.attendees.contains(currentUserId)

                if (isCreator) {
                    // Show Share button
                    SectionTitle("Share Event")

                    Surface(
                        color = Color.Blue,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Share Code: ${event.shareCode}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Use this code to invite others to your event. Share it with friends!",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val shareMessage =
                                    "Join my event '${event.title}' on iHost! Use the share code: ${event.shareCode}"
                                // Copy button
                                Button(
                                    onClick = { // Copy to clipboard
                                        // Get clipboard manager from context
                                        // (https://stackoverflow.com/questions/79692173/how-to-resolve-deprecated-clipboardmanager-in-jetpack-compose)
                                        // and (https://stackoverflow.com/questions/45255755/failed-to-use-android-context-clipboardmanager-to-clip-a-phone-number)
                                        // Apparently, they deprecated the WAY EASIER TO USE old ClipboardManager in favor of this bullshit..
                                        val clipBoardManager =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        // Create clip data
                                        val clipData =
                                            ClipData.newPlainText("Event share", shareMessage)
                                        // Set primary clip
                                        clipBoardManager.setPrimaryClip(clipData)
                                        // Show toast
                                        Toast.makeText(
                                            context,
                                            "Share code copied to clipboard",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1976D2)
                                    )
                                ) {
                                    Text("Copy invite message")
                                }

                                // Share button
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(
                                                intent,
                                                "Share event code"
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFC107),
                                        contentColor = Color(0xFF001D3D)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Share Event")
                                }
                            }
                        }
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
                                containerColor = Color(0xFF6B5B95)
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
                } else if (isAttending) {
                    // Attendee button - Leave
                    Button(
                        onClick = {
                            viewModel.leaveEvent(eventId)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Leave",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Leave Event", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Not attending - Join or Buy & Join button
                    if (event.free) {
                        // Free event - Join button
                        Button(
                            onClick = {
                                viewModel.joinEvent(eventId)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Join",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Join Event")
                        }
                    } else {
                        // Paid event - Buy & Join button
                        Button(
                            onClick = {
                                if (activity == null) {
                                    Toast.makeText(
                                        context,
                                        "Cannot process payment - activity not available",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                stripeViewModel.initiatePayment(
                                    activity = activity,
                                    eventId = eventId,
                                    onPaymentComplete = {
                                        // Payment successful - join event
                                        viewModel.joinEvent(eventId)
                                        Toast.makeText(
                                            context,
                                            "Payment successful! You've joined the event.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !stripeUiState.isProcessingPayment,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9C27B0)
                            )
                        ) {
                            if (stripeUiState.isProcessingPayment) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Buy & Join",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Buy & Join (${String.format("%.2f", event.price)} NOK)")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFC107))
            }
        }
    }
}

@Composable
fun EventDetailHeader(event: Event) {
    if (event.imageUrl == null) //TODO: CHANGE TO (event.imageUrl!=null) after firebase storege created
    {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            EventTimer(event.eventDate, event.eventTime)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6B5B95),
                                Color(0xFF4A3F7F)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {


                AsyncImage(
                    model = "22,",
                    contentDescription = "Selected event image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color.Black),

                    )

            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun EventDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF0C5CA7),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
fun EventTimer(eventDate: String?, eventTime: String?) {
    var timeRemaining by remember { mutableStateOf("") }
    var checkedTime="00:00"
    if(!eventTime.isNullOrBlank()){
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
                    String.format("%02d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format("%02d:%02d", hours, minutes)
                }
            } else if(diffMillis<1000*60*60*24){
                "Event Started!"
            }
            else{
                "Event ended!"
            }

            delay(1000) // Update every second
        }
    }

    Text(
        text = timeRemaining,
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)

    )
}