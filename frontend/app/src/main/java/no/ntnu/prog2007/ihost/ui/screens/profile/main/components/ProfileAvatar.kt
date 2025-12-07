package no.ntnu.prog2007.ihost.ui.screens.profile.main.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Profile avatar component
 *
 * Displays user's profile photo as circular avatar.
 * Shows placeholder icon if no photo available.
 * Clickable to open avatar change dialog.
 *
 * @param photoUrl User's profile photo URL; null shows placeholder
 * @param isLoading Whether profile is being updated
 * @param onChangeAvatar Callback invoked when user clicks avatar to change it
 * @param modifier Optional Modifier for customizing layout
 */
@Composable
fun ProfileAvatar(
    photoUrl: String?,
    isLoading: Boolean,
    onChangeAvatar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(vertical = 12.dp)
            .size(150.dp)
            .clickable(enabled = !isLoading) { onChangeAvatar() }
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Edit/Camera Icon Overlay
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary,
            shadowElevation = 4.dp
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Change Avatar",
                modifier = Modifier
                    .padding(8.dp)
                    .size(26.dp),
                tint = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}
