package no.ntnu.prog2007.ihost.ui.screens.events.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Invites button
        Button(
            onClick = { onTabSelected(0) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTab == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                contentColor = if (selectedTab == 0) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (selectedTab == 0) 6.dp else 2.dp
            )
        ) {
            Text(
                "Invites",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
            )
        }

        // My events button
        Button(
            onClick = { onTabSelected(1) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTab == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                contentColor = if (selectedTab == 1) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (selectedTab == 1) 6.dp else 2.dp
            )
        ) {
            Text(
                "My events",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
