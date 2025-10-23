package no.ntnu.prog2007.ihost.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    object Login : Screen("login", "Logg inn")
    object SignUp : Screen("signup", "Registrer deg")
    object Events : Screen(
        "events",
        "Events",
        selectedIcon = Icons.Default.MailOutline,
        unselectedIcon = Icons.Default.MailOutline
    )
    object AddEvent : Screen(
        "add_event",
        "Nytt event",
        selectedIcon = Icons.Default.AddCircle,
        unselectedIcon = Icons.Default.AddCircle
    )
    object Profile : Screen(
        "profile",
        "Profil",
        selectedIcon = Icons.Default.Person,
        unselectedIcon = Icons.Default.Person
    )
    object EventDetail : Screen("event_detail/{eventId}", "Event detaljer") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
}
