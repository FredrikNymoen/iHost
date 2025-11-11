package no.ntnu.prog2007.ihost.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    onLogOut: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val eventUiState by eventViewModel.uiState.collectAsState()
    val user = uiState.currentUser
    val userProfile = uiState.userProfile
    val isProfileLoading = uiState.isProfileLoading

    // Dialog states
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangeAvatarDialog by remember { mutableStateOf(false) }

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
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
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
                    .padding(top = 32.dp, bottom = 16.dp)
                    .size(150.dp)
                    .clickable { showChangeAvatarDialog = true }
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
                    .clickable { showEditNameDialog = true }
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

            // 3. Email (read-only)
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

            // 4. Statistics Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Events Created",
                    count = eventsCreated,
                    icon = Icons.Default.Event,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Invitations",
                    count = eventsInvitedTo,
                    icon = Icons.Default.MailOutline,
                    modifier = Modifier.weight(1f)
                )
            }

            // 5. Profile Info Card
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
                        label = "Username",
                        value = userProfile.username,
                        icon = Icons.Default.Person
                    )

                    /**
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    */

                    if (userProfile.phoneNumber != null) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
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

    if (showChangeAvatarDialog) {
        ChangeAvatarDialog(
            onDismiss = { showChangeAvatarDialog = false },
            onImageSelected = { uri ->
                // TODO: Upload image to backend and get URL
                // For now, we'll just use the URI as a string
                authViewModel.updateUserProfile(photoUrl = uri.toString())
                showChangeAvatarDialog = false
            }
        )
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
    onImageSelected: (Uri) -> Unit
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Change Profile Picture",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Select a new profile picture from your gallery.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Choose Image")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
