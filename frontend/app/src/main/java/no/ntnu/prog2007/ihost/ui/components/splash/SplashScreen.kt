package no.ntnu.prog2007.ihost.ui.components.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Custom splash screen with "iHost" branding
 */
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
