package no.ntnu.prog2007.ihost.ui.screens.profile.main

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.ntnu.prog2007.ihost.ui.screens.profile.main.components.ChangeAvatarDialog
import no.ntnu.prog2007.ihost.ui.screens.profile.main.components.EditNameDialog
import no.ntnu.prog2007.ihost.ui.screens.profile.main.components.ErrorState
import no.ntnu.prog2007.ihost.ui.screens.profile.main.components.FriendsSection
import no.ntnu.prog2007.ihost.ui.screens.profile.main.components.LoadingOverlay
import no.ntnu.prog2007.ihost.ui.screens.profile.main.components.LogOutButton
import no.ntnu.prog2007.ihost.ui.screens.profile.main.components.ProfileAvatar
import no.ntnu.prog2007.ihost.ui.screens.profile.main.components.ProfileHeader
import no.ntnu.prog2007.ihost.ui.screens.profile.main.components.ProfileStatistics
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel
import java.io.File

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    friendViewModel: FriendViewModel,
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

    // Load profile data only if not already loaded
    LaunchedEffect(user) {
        if (user != null) {
            if (userProfile == null && !isProfileLoading) {
                authViewModel.loadUserProfile()
            }
            eventViewModel.ensureEventsLoaded()
            if (friendUiState.friends.isEmpty() && !friendUiState.isLoading) {
                friendViewModel.loadFriendships()
            }
        }
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
            LoadingOverlay(message = "Loading profile...")
        } else if (uiState.errorMessage?.contains("account has been deleted") == true ||
            uiState.errorMessage?.contains("sign in again") == true) {
            // Account deleted or requires re-authentication
            ErrorState(
                message = "Please sign in again.",
                actionButtonText = "Return to Login.",
                onAction = onLogOut
            )
        } else if (user != null && userProfile != null) {
            // 1. Avatar Section
            ProfileAvatar(
                photoUrl = userProfile.photoUrl,
                isLoading = uiState.isLoading,
                onChangeAvatar = { showChangeAvatarDialog = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Profile Header (Name, Username, Email)
            ProfileHeader(
                firstName = userProfile.firstName,
                lastName = userProfile.lastName,
                username = userProfile.username,
                email = user.email,
                isLoading = uiState.isLoading,
                onEditName = { showEditNameDialog = true }
            )

            // 3. Statistics
            ProfileStatistics(
                eventsCreated = eventsCreated,
                eventsInvitedTo = eventsInvitedTo
            )

            // 5. Friends Section
            FriendsSection(
                friendViewModel = friendViewModel,
                authViewModel = authViewModel,
                friendUiState = friendUiState,
                onNavigateToAddFriend = onNavigateToAddFriend,
                onNavigateToFriendsList = onNavigateToFriendsList
            )

            Spacer(modifier = Modifier.weight(1f))

            // 7. Log Out Button
            LogOutButton(
                onClick = {
                    authViewModel.signOut()
                    onLogOut()
                }
            )
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
                            delay(500)
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
            val imageFile = File(
                context.cacheDir,
                "profile_camera_${System.currentTimeMillis()}.jpg"
            )
            val uri = FileProvider.getUriForFile(
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
                        delay(500)
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
            LoadingOverlay()
        }
    }
}