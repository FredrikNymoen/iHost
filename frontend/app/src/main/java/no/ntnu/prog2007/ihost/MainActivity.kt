package no.ntnu.prog2007.ihost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import no.ntnu.prog2007.ihost.ui.components.BottomNavigationBar
import no.ntnu.prog2007.ihost.ui.navigation.NavigationGraph
import no.ntnu.prog2007.ihost.ui.navigation.Screen
import no.ntnu.prog2007.ihost.ui.theme.DarkBlue
import no.ntnu.prog2007.ihost.ui.theme.IHostTheme
import no.ntnu.prog2007.ihost.ui.theme.LightBlue
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            IHostTheme {
                IHostApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IHostApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val eventViewModel: EventViewModel = viewModel { EventViewModel(authViewModel) }

    val authUiState by authViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Initial start destination
    val startDestination = Screen.Login.route

    // Navigate when auth state changes
    LaunchedEffect(authUiState.isLoggedIn, authUiState.isLoading) {
        if (authUiState.isLoggedIn && !authUiState.isLoading) {
            // User logged in/signed up and loading is done - navigate to Events
            navController.navigate(Screen.Events.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else if (!authUiState.isLoggedIn && currentRoute !in listOf(Screen.Login.route, Screen.SignUp.route)) {
            // User logged out - navigate back to Login
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Screens for bottom navigation (only shown when logged in)
    val bottomNavScreens = listOf(
        Screen.Events,
        Screen.AddEvent,
        Screen.Profile
    )

    // Check if we should show bottom navigation
    val shouldShowBottomNav = currentRoute in bottomNavScreens.map { it.route }
    val shouldShowTopBar = currentRoute != Screen.Login.route &&
                           currentRoute != Screen.SignUp.route &&
                           currentRoute?.startsWith("event_detail") != true

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(LightBlue, DarkBlue)
    )

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Scaffold(
            topBar = {
                if (shouldShowTopBar) {
                    TopAppBar(
                        title = {
                            Text(
                                bottomNavScreens.find { it.route == currentRoute }?.title ?: "",
                                color = Color(0xFFFFC107)
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DarkBlue
                        )
                    )
                }
            },
            bottomBar = {
                if (shouldShowBottomNav) {
                    BottomNavigationBar(
                        navController = navController,
                        screens = bottomNavScreens,
                        currentRoute = currentRoute
                    )
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            NavigationGraph(
                navController = navController,
                authViewModel = authViewModel,
                eventViewModel = eventViewModel,
                modifier = Modifier.padding(padding),
                startDestination = startDestination
            )
        }
    }
}