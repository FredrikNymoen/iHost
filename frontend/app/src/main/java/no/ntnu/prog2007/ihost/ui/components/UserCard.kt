package no.ntnu.prog2007.ihost.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import no.ntnu.prog2007.ihost.data.model.domain.User

/**
 * Reusable user card component that displays user information with optional actions
 * Used across the app for consistency
 */
@Composable
fun UserCard(
    user: User,
    modifier: Modifier = Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    avatarSize: androidx.compose.ui.unit.Dp = 48.dp,
    showCard: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val content = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick)
                    else Modifier
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // User avatar
                if (user.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.firstName.firstOrNull()?.uppercase() ?: "?",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = (avatarSize.value / 2.4).sp
                        )
                    }
                }

                Column {
                    Text(
                        text = "${user.firstName} ${user.lastName ?: ""}".trim(),
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "@${user.username}",
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            // Trailing content (buttons, icons, etc.)
            trailingContent?.invoke()
        }
    }

    if (showCard) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            content()
        }
    }
}

/**
 * User card with a single icon button action
 */
@Composable
fun UserCardWithIconAction(
    user: User,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    iconDescription: String,
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    showCard: Boolean = true
) {
    UserCard(
        user = user,
        modifier = modifier,
        backgroundColor = backgroundColor,
        textColor = textColor,
        showCard = showCard,
        trailingContent = {
            IconButton(
                onClick = onIconClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    tint = iconTint
                )
            }
        }
    )
}

/**
 * User card with two icon button actions
 */
@Composable
fun UserCardWithTwoActions(
    user: User,
    firstIcon: ImageVector,
    firstIconTint: androidx.compose.ui.graphics.Color,
    firstIconDescription: String,
    onFirstIconClick: () -> Unit,
    secondIcon: ImageVector,
    secondIconTint: androidx.compose.ui.graphics.Color,
    secondIconDescription: String,
    onSecondIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    showCard: Boolean = true
) {
    UserCard(
        user = user,
        modifier = modifier,
        backgroundColor = backgroundColor,
        textColor = textColor,
        showCard = showCard,
        trailingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onFirstIconClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = firstIcon,
                        contentDescription = firstIconDescription,
                        tint = firstIconTint
                    )
                }
                IconButton(
                    onClick = onSecondIconClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = secondIcon,
                        contentDescription = secondIconDescription,
                        tint = secondIconTint
                    )
                }
            }
        }
    )
}
