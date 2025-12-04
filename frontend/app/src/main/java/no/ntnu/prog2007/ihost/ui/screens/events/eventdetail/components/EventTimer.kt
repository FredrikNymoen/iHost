package no.ntnu.prog2007.ihost.ui.screens.events.eventdetail.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
