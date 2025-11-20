package no.ntnu.prog2007.ihost.ui.screens.profile

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import no.ntnu.prog2007.ihost.data.model.getOtherUserId
import no.ntnu.prog2007.ihost.ui.components.UserCardWithTwoActions
import no.ntnu.prog2007.ihost.ui.components.UserCardWithIconAction
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    friendViewModel: no.ntnu.prog2007.ihost.viewmodel.FriendViewModel,
    onLogOut: () -> Unit,
    onNavigateToAddFriend: () -> Unit,
    onNavigateToFriendsList: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by authViewModel.uiState.collectAsState()
    val eventUiState by eventViewModel.uiState.collectAsState()
    val friendUiState by friendViewModel.uiState.collectAsState()
    val user = uiState.currentUser
    val userProfile = uiState.userProfile
    val isProfileLoading = uiState.isProfileLoading

    // Dialog states
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangeAvatarDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageKey by remember { mutableStateOf(0) }

    // Calculate statistics
    val eventsCreated = remember(eventUiState.events) {
        eventUiState.events.count { it.userRole == "CREATOR" }
    }
    val eventsInvitedTo = remember(eventUiState.events) {
        eventUiState.events.count { it.userRole != "CREATOR" }
    }

    // Load profile data when the screen is displayed
    LaunchedEffect(Unit) {
        authViewModel.loadUserProfile()
        eventViewModel.ensureEventsLoaded()
        friendViewModel.loadFriendships()
    }

    // Gradient background matching MainActivity
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Show loading indicator if profile is loading
        if (isProfileLoading || (user != null && userProfile == null)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading profile...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (uiState.errorMessage?.contains("account has been deleted") == true ||
            uiState.errorMessage?.contains("sign in again") == true) {
            // Account deleted or requires re-authentication
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Please sign in again.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLogOut,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Return to Login.")
                }
            }
        } else if (user != null && userProfile != null) {
            // 1. Avatar Section with Edit Icon
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(150.dp)
                    .clickable(enabled = !uiState.isLoading) { showChangeAvatarDialog = true }
            ) {
                if (userProfile.photoUrl != null) {
                    AsyncImage(
                        model = userProfile.photoUrl,
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

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Name Section with Edit Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(enabled = !uiState.isLoading) { showEditNameDialog = true }
                    .padding(8.dp)
            ) {
                Text(
                    text = "${userProfile.firstName} ${userProfile.lastName ?: ""}".trim(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Name",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // 3. Username (read-only)
            Text(
                text = "@${userProfile.username}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 4. Email (read-only)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = user.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 5. Friends Section
            FriendsSection(
                friendViewModel = friendViewModel,
                authViewModel = authViewModel,
                friendUiState = friendUiState,
                onNavigateToAddFriend = onNavigateToAddFriend,
                onNavigateToFriendsList = onNavigateToFriendsList
            )

            // 6. Statistics Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Events Created (All Time)",
                    count = eventsCreated,
                    icon = Icons.Default.Event,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Invitations (All Time)",
                    count = eventsInvitedTo,
                    icon = Icons.Default.MailOutline,
                    modifier = Modifier.weight(1f)
                )
            }

            // 7. Profile Info Card (Phone Number)
            if (userProfile.phoneNumber != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        ProfileInfoItem(
                            label = "Phone Number",
                            value = userProfile.phoneNumber,
                            icon = Icons.Default.Phone
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 6. Log Out Button
            Button(
                onClick = {
                    authViewModel.signOut()
                    onLogOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = "Not logged in",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    // Dialogs
    if (showEditNameDialog && userProfile != null) {
        EditNameDialog(
            currentFirstName = userProfile.firstName,
            currentLastName = userProfile.lastName ?: "",
            onDismiss = { showEditNameDialog = false },
            onSave = { firstName, lastName ->
                authViewModel.updateUserProfile(
                    firstName = firstName,
                    lastName = lastName
                )
                showEditNameDialog = false
            }
        )
    }

    // Camera launcher - must be declared before cameraPermissionLauncher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) {
            selectedImageUri = null
            Log.d("ProfileCamera", "Photo capture cancelled or failed")
        } else {
            Log.d("ProfileCamera", "Photo captured: $selectedImageUri")
            imageKey++
            // Upload the photo when successfully captured
            selectedImageUri?.let { uri ->
                coroutineScope.launch {
                    try {
                        val photoUrl = authViewModel.uploadProfilePhoto(context, uri)
                        if (photoUrl != null) {
                            // Wait a bit for backend to finish updating, then reload
                            kotlinx.coroutines.delay(500)
                            authViewModel.loadUserProfile()
                            Log.d("ProfileScreen", "Profile photo uploaded: $photoUrl")
                        } else {
                            Log.e("ProfileScreen", "Failed to upload profile photo")
                        }
                    } finally {
                        selectedImageUri = null
                    }
                }
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val imageFile = java.io.File(
                context.cacheDir,
                "profile_camera_${System.currentTimeMillis()}.jpg"
            )
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile
            )
            selectedImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            Log.d("ProfileCamera", "Camera permission denied")
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Log.d("ProfilePhotoPicker", "Selected URI: $uri")
            // Upload the photo when selected from gallery
            coroutineScope.launch {
                try {
                    val photoUrl = authViewModel.uploadProfilePhoto(context, uri)
                    if (photoUrl != null) {
                        // Wait a bit for backend to finish updating, then reload
                        kotlinx.coroutines.delay(500)
                        authViewModel.loadUserProfile()
                        Log.d("ProfileScreen", "Profile photo uploaded: $photoUrl")
                    } else {
                        Log.e("ProfileScreen", "Failed to upload profile photo")
                    }
                } finally {
                    selectedImageUri = null
                }
            }
        }
    }

    if (showChangeAvatarDialog) {
        ChangeAvatarDialog(
            onDismiss = { showChangeAvatarDialog = false },
            onTakePhoto = {
                showChangeAvatarDialog = false
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onSelectFromGallery = {
                showChangeAvatarDialog = false
                galleryLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        )
    }

        // Show loading overlay when uploading/updating
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Updating profile...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onTertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun EditNameDialog(
    currentFirstName: String,
    currentLastName: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var firstName by remember { mutableStateOf(currentFirstName) }
    var lastName by remember { mutableStateOf(currentLastName) }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Name",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (firstName.isNotBlank()) {
                        onSave(firstName, lastName.ifBlank { null } ?: "")
                    }
                },
                enabled = firstName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangeAvatarDialog(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onSelectFromGallery: () -> Unit
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Change Profile Picture",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "How would you like to add a profile picture?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTakePhoto,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Take photo",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Photo")
                }
                Button(
                    onClick = onSelectFromGallery,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Select from gallery",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

@Composable
fun FriendsSection(
    friendViewModel: no.ntnu.prog2007.ihost.viewmodel.FriendViewModel,
    authViewModel: AuthViewModel,
    friendUiState: no.ntnu.prog2007.ihost.viewmodel.FriendUiState,
    onNavigateToAddFriend: () -> Unit,
    onNavigateToFriendsList: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Friends section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // View Friends button
            Button(
                onClick = onNavigateToFriendsList,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "${friendUiState.friends.size} Friends",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "View Friends",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Add Friend button
            Button(
                onClick = onNavigateToAddFriend,
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Friend",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Pending friend requests (received)
        if (friendUiState.pendingRequests.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Friend Requests (${friendUiState.pendingRequests.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    friendUiState.pendingRequests.forEach { friendship ->
                        val requesterUser = friendUiState.userDetailsMap[friendship.user1Id]
                        if (requesterUser != null) {
                            UserCardWithTwoActions(
                                user = requesterUser,
                                firstIcon = Icons.Default.Check,
                                firstIconTint = MaterialTheme.colorScheme.primary,
                                firstIconDescription = "Accept",
                                onFirstIconClick = {
                                    friendViewModel.acceptFriendRequest(
                                        friendshipId = friendship.id,
                                        onSuccess = {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Accepted friend request from ${requesterUser.firstName}",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { error ->
                                            android.widget.Toast.makeText(
                                                context,
                                                "Error: $error",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                },
                                secondIcon = Icons.Default.Close,
                                secondIconTint = MaterialTheme.colorScheme.error,
                                secondIconDescription = "Decline",
                                onSecondIconClick = {
                                    friendViewModel.declineFriendRequest(
                                        friendshipId = friendship.id,
                                        onSuccess = {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Declined friend request",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { error ->
                                            android.widget.Toast.makeText(
                                                context,
                                                "Error: $error",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                },
                                backgroundColor = MaterialTheme.colorScheme.secondary,
                                textColor = MaterialTheme.colorScheme.onSecondary,
                                showCard = false
                            )
                            if (friendship != friendUiState.pendingRequests.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sent friend requests (pending)
        if (friendUiState.sentRequests.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Sent Requests (${friendUiState.sentRequests.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    friendUiState.sentRequests.forEach { friendship ->
                        val recipientUser = friendUiState.userDetailsMap[friendship.user2Id]
                        if (recipientUser != null) {
                            UserCardWithIconAction(
                                user = recipientUser,
                                icon = Icons.Default.Close,
                                iconTint = MaterialTheme.colorScheme.error,
                                iconDescription = "Cancel request",
                                onIconClick = {
                                    friendViewModel.removeFriend(
                                        friendshipId = friendship.id,
                                        onSuccess = {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Cancelled friend request",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { error ->
                                            android.widget.Toast.makeText(
                                                context,
                                                "Error: $error",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                },
                                backgroundColor = MaterialTheme.colorScheme.tertiary,
                                textColor = MaterialTheme.colorScheme.onTertiary,
                                showCard = false
                            )
                            if (friendship != friendUiState.sentRequests.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}