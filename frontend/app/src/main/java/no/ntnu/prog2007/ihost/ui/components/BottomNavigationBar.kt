package no.ntnu.prog2007.ihost.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import no.ntnu.prog2007.ihost.ui.navigation.Screen
import no.ntnu.prog2007.ihost.ui.theme.DarkBlue
import no.ntnu.prog2007.ihost.ui.theme.Gold

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    screens: List<Screen>,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = DarkBlue
    ) {
        screens.forEach { screen ->
            val isSelected = currentRoute == screen.route
            val icon = if (isSelected) screen.selectedIcon else screen.unselectedIcon

            if (icon != null) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            icon,
                            contentDescription = screen.title
                        )
                    },
                    label = { Text(screen.title) },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Gold,
                        selectedTextColor = Gold,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
