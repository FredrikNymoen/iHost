package no.ntnu.prog2007.ihost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import no.ntnu.prog2007.ihost.ui.components.layout.AppScaffold
import no.ntnu.prog2007.ihost.ui.components.splash.SplashScreen
import no.ntnu.prog2007.ihost.ui.navigation.config.Destination
import no.ntnu.prog2007.ihost.ui.navigation.state.DataLoadingEffects
import no.ntnu.prog2007.ihost.ui.navigation.state.NavigationEffects
import no.ntnu.prog2007.ihost.ui.navigation.state.rememberNavigationState
import no.ntnu.prog2007.ihost.ui.theme.IHostTheme
import no.ntnu.prog2007.ihost.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            IHostTheme {
                if (showSplash) {
                    SplashScreen { showSplash = false }
                } else {
                    IHostApp()
                }
            }
        }
    }
}

@Composable
fun IHostApp() {
    val navController = rememberNavController()
    val navigationState = rememberNavigationState(navController)

    // ViewModels
    val authViewModel: AuthViewModel = viewModel()
    val eventViewModel: EventViewModel = viewModel()
    val friendViewModel: FriendViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    val authUiState by authViewModel.uiState.collectAsState()

    // Determine start destination based on login state
    val startDestination = if (authUiState.isLoggedIn) {
        Destination.Events.route
    } else {
        Destination.Welcome.route
    }

    // Handle initial data loading
    DataLoadingEffects(
        authUiState = authUiState,
        eventViewModel = eventViewModel,
        userViewModel = userViewModel
    )

    // Handle navigation based on auth state
    NavigationEffects(
        authUiState = authUiState,
        navigationState = navigationState,
        eventViewModel = eventViewModel,
        friendViewModel = friendViewModel,
        userViewModel = userViewModel
    )

    // Also clear auth data on logout
    LaunchedEffect(authUiState.isLoggedIn) {
        if (!authUiState.isLoggedIn) {
            authViewModel.clearAuthData()
        }
    }

    // Render app scaffold
    AppScaffold(
        navController = navController,
        navigationState = navigationState,
        authViewModel = authViewModel,
        eventViewModel = eventViewModel,
        friendViewModel = friendViewModel,
        userViewModel = userViewModel,
        startDestination = startDestination
    )
}