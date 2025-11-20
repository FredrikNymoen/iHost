package no.ntnu.prog2007.ihost.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    object Login : Screen("login", "Log in")
    object SignUp : Screen("signup", "Register")
    object PersonalInfo : Screen("personal_info", "Personal info")
    object Events : Screen(
        "events",
        "Events",
        selectedIcon = Icons.Default.Drafts,
        unselectedIcon = Icons.Default.MailOutline
    )

    object AddEvent : Screen(
        "add_event",
        "Add event",
        selectedIcon = Icons.Default.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircleOutline
    )

    object EditEvent : Screen("edit_event/{eventId}", "Edit Event") {
        fun createRoute(eventId: String) = "edit_event/$eventId"
    }

    object Profile : Screen(
        "profile",
        "Profile",
        selectedIcon = Icons.Default.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    object EventDetail : Screen("event_detail/{eventId}", "Event detaljer") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }

    object InviteUsers : Screen("invite_users/{eventId}", "Invite Users") {
        fun createRoute(eventId: String) = "invite_users/$eventId"
    }

    object AddFriend : Screen("add_friend", "Add Friend")
    object FriendsList : Screen("friends_list", "Friends")
}
