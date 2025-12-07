package no.ntnu.prog2007.ihost.ui.components.events

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

/**
 * Event image display and selection component
 *
 * Displays an event image with options to add, change, or remove it. Shows either
 * a selected local image URI or an existing image URL. When no image is present,
 * displays a placeholder with add image prompt.
 *
 * @param selectedImageUri Local image URI from device (camera/gallery)
 * @param existingImageUrl Remote image URL (from server/cloud storage)
 * @param imageKey Key to force image recomposition when changed
 * @param placeholderText Text shown when no image is selected
 * @param onAddImageClick Callback invoked when add/change image area is clicked
 * @param onRemoveImage Callback invoked when remove button is clicked
 */
@Composable
fun EventImageSection(
    selectedImageUri: Uri?,
    existingImageUrl: String? = null,
    imageKey: Int = 0,
    placeholderText: String = "Tap to add image",
    onAddImageClick: () -> Unit,
    onRemoveImage: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(150.dp)
            .background(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        val imageToShow = selectedImageUri ?: existingImageUrl

        if (imageToShow != null) {
            key(imageKey) {
                AsyncImage(
                    model = imageToShow,
                    contentDescription = "Event image",
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            // Show remove button and add image button overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onAddImageClick)
            )

            IconButton(
                onClick = onRemoveImage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(50))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove image",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onAddImageClick)
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = "Add image",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    placeholderText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
