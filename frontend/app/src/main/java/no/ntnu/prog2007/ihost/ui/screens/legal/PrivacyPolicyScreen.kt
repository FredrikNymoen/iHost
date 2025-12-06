package no.ntnu.prog2007.ihost.ui.screens.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Last Updated: December 6, 2025",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("1. Introduction")
            SectionBody(
                "This Privacy Policy describes how iHost (\"we\", \"our\", or \"the App\") collects, uses, and protects " +
                "your personal information. We are committed to protecting your privacy and ensuring the security of your data."
            )

            SectionTitle("2. Information We Collect")
            SectionBody(
                "We collect the following personal information when you register:\n\n" +
                "Required Information:\n" +
                "• Email address\n" +
                "• Password (encrypted and securely stored by Firebase Authentication)\n" +
                "• First name\n" +
                "• Last name\n" +
                "• Username (4-12 characters)\n\n" +
                "Optional Information:\n" +
                "• Profile photo\n" +
                "• Event images\n" +
                "• Event details (title, description, date, time, location)\n\n" +
                "Automatically Collected Information:\n" +
                "• Account creation and update timestamps\n" +
                "• Event participation status (creator, accepted, pending, declined)\n" +
                "• Friendship connections and request timestamps"
            )

            SectionTitle("3. How We Use Your Information")
            SectionBody(
                "Your information is used to:\n" +
                "• Create and manage your user account\n" +
                "• Enable event creation and management\n" +
                "• Facilitate friend connections and event invitations\n" +
                "• Display your profile to other users you connect with\n" +
                "• Send event notifications and updates\n" +
                "• Improve and optimize the App experience\n" +
                "• Ensure security and prevent abuse"
            )

            SectionTitle("4. Data Storage and Security")
            SectionBody(
                "Your data is stored securely using industry-standard services:\n\n" +
                "Firebase Authentication:\n" +
                "• Passwords are encrypted and never stored in plain text\n" +
                "• Google-managed security infrastructure\n" +
                "• Secure authentication tokens (JWT)\n\n" +
                "Firebase Firestore:\n" +
                "• User profiles, events, friendships, and event participation data\n" +
                "• Encrypted data transmission (HTTPS)\n" +
                "• Access controlled by authentication\n\n" +
                "Cloudinary:\n" +
                "• Event and profile images\n" +
                "• Secure cloud storage with CDN distribution\n" +
                "• Automatic image optimization"
            )

            SectionTitle("5. Information Sharing and Visibility")
            SectionBody(
                "Your information is shared as follows:\n\n" +
                "Visible to All App Users:\n" +
                "• Username\n" +
                "• First name and last name\n" +
                "• Profile photo (if uploaded)\n\n" +
                "Visible to Your Friends:\n" +
                "• Same as above, plus easier discoverability for event invitations\n\n" +
                "Visible to Event Participants:\n" +
                "• Your name and profile photo for events you've joined or been invited to\n" +
                "• Your participation status (creator, accepted, pending, declined)\n\n" +
                "Not Shared with Third Parties:\n" +
                "• Your email address is never shared with other users\n" +
                "• Your password is encrypted and inaccessible to anyone, including us\n" +
                "• We do not sell your data to advertisers or third parties"
            )

            SectionTitle("6. Your Rights and Choices")
            SectionBody(
                "You have the right to:\n" +
                "• Access your personal information stored in the App\n" +
                "• Update or correct your profile information\n" +
                "• Delete your account and associated data\n" +
                "• Accept or decline friend requests\n" +
                "• Accept or decline event invitations\n" +
                "• Remove yourself from events\n" +
                "• Delete events you created (removes all associated data)"
            )

            SectionTitle("7. Data Retention")
            SectionBody(
                "We retain your data as follows:\n" +
                "• Active accounts: Data is retained indefinitely while your account is active\n" +
                "• Deleted accounts: Personal data is permanently removed from our systems\n" +
                "• Event data: Events and associated images are deleted when the event creator deletes the event\n" +
                "• Friendship data: Connection data is removed when either user ends the friendship"
            )

            SectionTitle("8. Children's Privacy")
            SectionBody(
                "iHost is not intended for users under the age of 13. We do not knowingly collect personal information " +
                "from children under 13. If we become aware that a child under 13 has provided us with personal information, " +
                "we will delete such information from our systems."
            )

            SectionTitle("9. Third-Party Services")
            SectionBody(
                "We use the following third-party services:\n\n" +
                "• Firebase (Google): Authentication and database services\n" +
                "  - Subject to Google's Privacy Policy\n" +
                "  - https://policies.google.com/privacy\n\n" +
                "• Cloudinary: Image storage and optimization\n" +
                "  - Subject to Cloudinary's Privacy Policy\n" +
                "  - https://cloudinary.com/privacy\n\n" +
                "These services have their own privacy policies and data handling practices."
            )

            SectionTitle("10. International Data Transfers")
            SectionBody(
                "Your data may be stored and processed in servers located in different countries, including:\n" +
                "• Firebase servers (Google Cloud Platform locations)\n" +
                "• Cloudinary CDN locations worldwide\n\n" +
                "By using the App, you consent to the transfer of your information to these locations."
            )

            SectionTitle("11. Changes to This Privacy Policy")
            SectionBody(
                "We may update this Privacy Policy from time to time. We will notify you of significant changes by:\n" +
                "• Updating the \"Last Updated\" date at the top of this policy\n" +
                "• In-app notifications for material changes\n\n" +
                "Your continued use of the App after changes constitutes acceptance of the updated policy."
            )

            SectionTitle("12. Contact Us")
            SectionBody(
                "If you have questions, concerns, or requests regarding this Privacy Policy or your personal data, " +
                "please contact us through the App's support channels or settings."
            )

            SectionTitle("13. Your Consent")
            SectionBody(
                "By using iHost, you consent to this Privacy Policy and agree to its terms regarding the collection, " +
                "use, and sharing of your information as described herein."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SectionBody(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
