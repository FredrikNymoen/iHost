package no.ntnu.prog2007.ihost.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onLogOut: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val user = uiState.currentUser
    val userProfile = uiState.userProfile
    val isProfileLoading = uiState.isProfileLoading

    // Load profile data when the screen is displayed
    LaunchedEffect(Unit) {
        authViewModel.loadUserProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile icon (shows regardless of loading state)
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 32.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        // Show loading indicator if profile is loading, else show profile info
        if (isProfileLoading || (user != null && userProfile == null)) {
            // Loading indicator
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
                        text = "Laster profil...",
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
        } else if (user != null) {
            Text(
                text = user.displayName ?: "Bruker",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = user.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    ProfileInfoItem(label = "Brukernavn", value = user.displayName ?: "N/A")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                    if (userProfile != null) {
                        val firstName = userProfile.firstName
                        val lastName = userProfile.lastName ?: ""
                        ProfileInfoItem(
                            "Navn",
                            if (lastName.isNotEmpty()) "$firstName $lastName" else firstName
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = DividerDefaults.Thickness,
                            color = DividerDefaults.color
                        )
                    }
                    ProfileInfoItem(label = "E-post", value = user.email ?: "N/A")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                    ProfileInfoItem(
                        label = "E-post bekreftet",
                        value = if (user.isEmailVerified) "Ja" else "Nei"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    authViewModel.signOut()
                    onLogOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Logg ut")
            }
        } else {
            Text(
                text = "Ikke logget inn",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
