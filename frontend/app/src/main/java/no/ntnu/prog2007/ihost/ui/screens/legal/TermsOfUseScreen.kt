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
fun TermsOfUseScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms of Use") },
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

            SectionTitle("1. Acceptance of Terms")
            SectionBody(
                "By accessing and using iHost (\"the App\"), you accept and agree to be bound by these Terms of Use. " +
                "If you do not agree to these terms, please do not use the App."
            )

            SectionTitle("2. Description of Service")
            SectionBody(
                "iHost is an event management application that allows users to:\n" +
                "• Create and organize social events\n" +
                "• Invite friends and manage attendees\n" +
                "• Share event details and images\n" +
                "• Connect with other users through friendships"
            )

            SectionTitle("3. User Accounts and Registration")
            SectionBody(
                "To use iHost, you must create an account by providing:\n" +
                "• Email address\n" +
                "• Password (securely stored by Firebase Authentication)\n" +
                "• First name and last name\n" +
                "• Username (4-12 characters)\n\n" +
                "You are responsible for maintaining the confidentiality of your account credentials and for all activities " +
                "that occur under your account."
            )

            SectionTitle("4. User Responsibilities")
            SectionBody(
                "You agree to:\n" +
                "• Provide accurate and truthful information\n" +
                "• Use the App in compliance with all applicable laws\n" +
                "• Not impersonate others or create false identities\n" +
                "• Not upload harmful, offensive, or inappropriate content\n" +
                "• Respect the privacy and rights of other users\n" +
                "• Not use the App for spam, harassment, or malicious purposes"
            )

            SectionTitle("5. Events and Content")
            SectionBody(
                "When you create an event or upload content:\n" +
                "• You retain ownership of your content\n" +
                "• You grant iHost a license to store, display, and distribute your content as necessary to operate the service\n" +
                "• You are responsible for the accuracy of event information\n" +
                "• You must not create events for illegal activities\n" +
                "• Event images are stored via Cloudinary and must comply with their terms of service"
            )

            SectionTitle("6. Friendships and Invitations")
            SectionBody(
                "The App allows you to:\n" +
                "• Send and receive friend requests\n" +
                "• Invite friends to events\n" +
                "• Accept or decline event invitations\n\n" +
                "You understand that friendship connections and event invitations are managed through the App's systems."
            )

            SectionTitle("7. Data Storage")
            SectionBody(
                "Your data is stored using:\n" +
                "• Firebase Firestore for user profiles, events, friendships, and event participation\n" +
                "• Firebase Authentication for secure password management\n" +
                "• Cloudinary for event image storage\n\n" +
                "Please refer to our Privacy Policy for detailed information about data handling."
            )

            SectionTitle("8. Termination")
            SectionBody(
                "We reserve the right to suspend or terminate your account if you:\n" +
                "• Violate these Terms of Use\n" +
                "• Engage in fraudulent or illegal activities\n" +
                "• Abuse the service or harm other users\n\n" +
                "You may delete your account at any time through the App settings."
            )

            SectionTitle("9. Disclaimer of Warranties")
            SectionBody(
                "iHost is provided \"as is\" without warranties of any kind. We do not guarantee:\n" +
                "• Uninterrupted or error-free service\n" +
                "• Accuracy of event information provided by other users\n" +
                "• Security against unauthorized access\n\n" +
                "Use the App at your own risk."
            )

            SectionTitle("10. Limitation of Liability")
            SectionBody(
                "iHost and its developers shall not be liable for:\n" +
                "• Any indirect, incidental, or consequential damages\n" +
                "• Loss of data or content\n" +
                "• Actions or conduct of other users\n" +
                "• Issues arising from third-party services (Firebase, Cloudinary)"
            )

            SectionTitle("11. Changes to Terms")
            SectionBody(
                "We reserve the right to modify these Terms of Use at any time. Changes will be effective immediately upon " +
                "posting. Your continued use of the App after changes constitutes acceptance of the modified terms."
            )

            SectionTitle("12. Contact Information")
            SectionBody(
                "If you have questions about these Terms of Use, please contact us through the App's support channels."
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
