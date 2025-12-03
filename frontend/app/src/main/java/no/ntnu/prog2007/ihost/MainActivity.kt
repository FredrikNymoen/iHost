package no.ntnu.prog2007.ihost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import no.ntnu.prog2007.ihost.service.StripePaymentService
import no.ntnu.prog2007.ihost.ui.components.layout.AppHeader
import no.ntnu.prog2007.ihost.ui.components.layout.BottomNavigationBar
import no.ntnu.prog2007.ihost.ui.navigation.AppNavHost
import no.ntnu.prog2007.ihost.ui.navigation.Destination
import no.ntnu.prog2007.ihost.ui.theme.IHostTheme
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import no.ntnu.prog2007.ihost.viewmodel.StripeViewModel
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Stripe PaymentSheet BEFORE setContent
        // Dette er KRITISK - må gjøres i onCreate() før aktiviteten blir RESUMED
        StripePaymentService.initializePaymentSheet(this)

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            IHostTheme {
                if (showSplash) {
                    // Custom splash screen with "iHost" text
                    SplashScreen {
                        showSplash = false
                    }
                } else {
                    IHostApp()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // Show splash for a short duration
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(800) // 0.8 second
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFE5DC)), // WarmCream color
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "iHost",
            fontSize = 120.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B5446), // RichBrown color
            letterSpacing = (-0.1).sp
        )
    }
}

@Composable
fun IHostApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val eventViewModel: EventViewModel = viewModel()
    val stripeViewModel: StripeViewModel = viewModel()
    val friendViewModel: FriendViewModel = viewModel()

    val authUiState by authViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Track if we've already handled initial navigation
    var hasNavigatedOnLogin by remember { mutableStateOf(false) }

    // Determine start destination based on login state
    val startDestination = if (authUiState.isLoggedIn) {
        Destination.Events.route
    } else {
        Destination.Login.route
    }

    // Load events on app start if already logged in
    LaunchedEffect(Unit) {
        if (authUiState.isLoggedIn) {
            eventViewModel.ensureEventsLoaded()
        }
    }

    // Navigate when auth state changes
    LaunchedEffect(authUiState.isLoggedIn, authUiState.isLoading, currentRoute) {
        if (authUiState.isLoggedIn && !authUiState.isLoading) {
            // User just logged in/signed up and loading is done
            // Navigate if we're on Login/SignUp screen
            if (currentRoute in listOf(Destination.Login.route, Destination.SignUp.route, Destination.PersonalInfo.route)) {
                if (!hasNavigatedOnLogin) {
                    hasNavigatedOnLogin = true
                    // Load fresh events for the new user
                    eventViewModel.loadEvents()
                    // Navigate to Events
                    navController.navigate(Destination.Events.route) {
                        popUpTo(Destination.Login.route) { inclusive = true }
                    }
                }
            }
        } else if (!authUiState.isLoggedIn) {
            // Reset flag when user is logged out
            hasNavigatedOnLogin = false
            // If user logged out and not on auth screens, navigate back to Login
            if (currentRoute != null && currentRoute !in listOf(Destination.Login.route, Destination.SignUp.route, Destination.PersonalInfo.route)) {
                eventViewModel.clearEvents()
                navController.navigate(Destination.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // Screens for bottom navigation (only shown when logged in)
    val bottomNavScreens = listOf(
        Destination.Events,
        Destination.AddEvent,
        Destination.Profile
    )

    // Screens that should show header
    val headerScreens = listOf(
        Destination.Events,
        Destination.AddEvent,
        Destination.Profile
    )

    // Check if we should show bottom navigation and header
    val shouldShowBottomNav = currentRoute in bottomNavScreens.map { it.route }
    val shouldShowHeader = currentRoute in headerScreens.map { it.route }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Scaffold(
            topBar = {
                if (shouldShowHeader) {
                    AppHeader()
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
            AppNavHost(
                navController = navController,
                authViewModel = authViewModel,
                eventViewModel = eventViewModel,
                stripeViewModel = stripeViewModel,
                friendViewModel = friendViewModel,
                modifier = Modifier.padding(padding),
                startDestination = startDestination
            )
        }
    }
}