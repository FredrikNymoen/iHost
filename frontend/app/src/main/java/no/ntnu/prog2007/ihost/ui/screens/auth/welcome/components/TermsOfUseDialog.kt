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
fun TermsOfUseDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Terms of Use",
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

                SectionTitle("1. Acceptance of Terms")
                SectionBody(
                    "By accessing and using iHost (\"the App\"), you accept and agree to be bound by these Terms of Use."
                )

                SectionTitle("2. Description of Service")
                SectionBody(
                    "iHost is an event management application that allows users to create and organize social events, invite friends, share event details and images, and connect with other users."
                )

                SectionTitle("3. User Accounts and Registration")
                SectionBody(
                    "To use iHost, you must create an account by providing: email address, password (securely stored by Firebase Authentication), first name and last name, and username (4-12 characters). You are responsible for maintaining the confidentiality of your account credentials."
                )

                SectionTitle("4. User Responsibilities")
                SectionBody(
                    "You agree to provide accurate information, use the App in compliance with applicable laws, not impersonate others, not upload harmful or inappropriate content, and respect the privacy and rights of other users."
                )

                SectionTitle("5. Events and Content")
                SectionBody(
                    "When you create an event or upload content, you retain ownership but grant iHost a license to store and display your content. Event images are stored via Cloudinary and must comply with their terms of service."
                )

                SectionTitle("6. Data Storage")
                SectionBody(
                    "Your data is stored using Firebase Firestore for user profiles and events, Firebase Authentication for secure password management, and Cloudinary for event image storage. Please refer to our Privacy Policy for detailed information."
                )

                SectionTitle("7. Termination")
                SectionBody(
                    "We reserve the right to suspend or terminate your account if you violate these Terms of Use, engage in fraudulent activities, or abuse the service. You may delete your account at any time."
                )

                SectionTitle("8. Disclaimer of Warranties")
                SectionBody(
                    "iHost is provided \"as is\" without warranties of any kind. We do not guarantee uninterrupted or error-free service, accuracy of event information, or security against unauthorized access."
                )

                SectionTitle("9. Limitation of Liability")
                SectionBody(
                    "iHost and its developers shall not be liable for any indirect, incidental, or consequential damages, loss of data, actions of other users, or issues from third-party services."
                )

                SectionTitle("10. Changes to Terms")
                SectionBody(
                    "We reserve the right to modify these Terms of Use at any time. Your continued use of the App after changes constitutes acceptance of the modified terms."
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
