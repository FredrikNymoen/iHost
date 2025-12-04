package no.ntnu.prog2007.ihost.ui.screens.events.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DateFilterBar(
    timeFilter: Int,
    onTimeFilterChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Current date
        Text(
            text = LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd.MM.yy")
            ),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Future/Past toggle with segmented control style
        Surface(
            modifier = Modifier.height(36.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.secondary
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Future button
                Surface(
                    onClick = { onTimeFilterChange(0) },
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = if (timeFilter == 0) MaterialTheme.colorScheme.primary else Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Future",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (timeFilter == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }

                // Past button
                Surface(
                    onClick = { onTimeFilterChange(1) },
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = if (timeFilter == 1) MaterialTheme.colorScheme.primary else Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Past",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (timeFilter == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}
