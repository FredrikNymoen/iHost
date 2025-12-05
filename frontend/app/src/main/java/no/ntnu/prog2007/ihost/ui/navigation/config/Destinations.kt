package no.ntnu.prog2007.ihost.ui.navigation.config

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Destinations for navigation in the app
 */
sealed class Destination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    // Auth destinations
    data object Welcome : Destination("welcome", "Welcome")
    data object Login : Destination("login", "Log in")
    data object SignUp : Destination("signup", "Register")
    data object PersonalInfo : Destination("personal_info", "Personal info")
    data object ForgotPassword : Destination("forgot_password", "Forgot Password")

    // Main app destinations
    data object Events : Destination(
        "events",
        "Events",
        selectedIcon = Icons.Default.Drafts,
        unselectedIcon = Icons.Default.MailOutline
    )

    data object AddEvent : Destination(
        "add_event",
        "Add event",
        selectedIcon = Icons.Default.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircleOutline
    )

    data object Profile : Destination(
        "profile",
        "Profile",
        selectedIcon = Icons.Default.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    // Detail destinations with arguments
    data object EventDetail : Destination("event_detail/{eventId}", "Event detaljer") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }

    data object EditEvent : Destination("edit_event/{eventId}", "Edit Event") {
        fun createRoute(eventId: String) = "edit_event/$eventId"
    }

    data object InviteUsers : Destination("invite_users/{eventId}", "Invite Users") {
        fun createRoute(eventId: String) = "invite_users/$eventId"
    }

    // Friend destinations
    data object AddFriend : Destination("add_friend", "Add Friend")
    data object FriendsList : Destination("friends_list", "Friends")
}
