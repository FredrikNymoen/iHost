package no.ntnu.prog2007.ihost.ui.screens.auth.welcome.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Privacy Policy",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Last Updated: December 6, 2025",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SectionTitle("1. Introduction")
                SectionBody(
                    "This Privacy Policy describes how iHost collects, uses, and protects your personal information. We are committed to protecting your privacy and ensuring the security of your data."
                )

                SectionTitle("2. Information We Collect")
                SectionBody(
                    "Required: Email address, password (encrypted by Firebase), first name, last name, and username (4-12 characters). Optional: Profile photo, event images, and event details. Automatically collected: Account timestamps, event participation status, and friendship connections."
                )

                SectionTitle("3. How We Use Your Information")
                SectionBody(
                    "Your information is used to create and manage your account, enable event creation, facilitate friend connections, display your profile to connected users, send event notifications, and ensure security."
                )

                SectionTitle("4. Data Storage and Security")
                SectionBody(
                    "Firebase Authentication: Passwords are encrypted and never stored in plain text with secure authentication tokens (JWT). Firebase Firestore: Stores user profiles, events, and friendships with encrypted transmission (HTTPS). Cloudinary: Stores event and profile images with automatic optimization."
                )

                SectionTitle("5. Information Sharing and Visibility")
                SectionBody(
                    "Visible to all users: Username, first/last name, and profile photo. Visible to event participants: Your name, profile photo, and participation status. Never shared: Your email address and password are never shared with other users or sold to third parties."
                )

                SectionTitle("6. Your Rights and Choices")
                SectionBody(
                    "You have the right to access, update, or delete your personal information, accept or decline friend requests and event invitations, remove yourself from events, and delete events you created."
                )

                SectionTitle("7. Data Retention")
                SectionBody(
                    "Active accounts: Data retained while account is active. Deleted accounts: Personal data is permanently removed. Event data: Deleted when event creator removes the event. Friendship data: Removed when either user ends the friendship."
                )

                SectionTitle("8. Children's Privacy")
                SectionBody(
                    "iHost is not intended for users under 13. We do not knowingly collect personal information from children under 13."
                )

                SectionTitle("9. Third-Party Services")
                SectionBody(
                    "We use Firebase (Google) for authentication and database services, and Cloudinary for image storage. These services have their own privacy policies."
                )

                SectionTitle("10. International Data Transfers")
                SectionBody(
                    "Your data may be stored and processed in servers located in different countries through Firebase servers and Cloudinary CDN locations worldwide."
                )

                SectionTitle("11. Changes to This Privacy Policy")
                SectionBody(
                    "We may update this Privacy Policy from time to time. We will notify you of significant changes by updating the \"Last Updated\" date and through in-app notifications for material changes."
                )

                SectionTitle("12. Your Consent")
                SectionBody(
                    "By using iHost, you consent to this Privacy Policy and agree to its terms regarding the collection, use, and sharing of your information as described herein."
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SectionBody(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
