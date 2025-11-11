package no.ntnu.prog2007.ihost.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RichBrown,
    secondary = WarmBrown,
    tertiary = WarmPeach,
    background = DeepBrown,
    surface = Color(0xFF5A4A3E),
    onPrimary = WarmWhite,
    onSecondary = DeepBrown,
    onTertiary = DeepBrown,
    onBackground = WarmCream,
    onSurface = WarmCream
)

private val LightColorScheme = lightColorScheme(
    primary = RichBrown,              // Rich dark brown - stands out
    secondary = WarmPeach,            // Light peach - soft accent
    tertiary = WarmCream,             // Cream - subtle accent
    background = WarmWhite,           // Almost white background
    surface = WarmCream,              // Cream surface
    onPrimary = WarmWhite,            // White text on primary (dark brown)
    onSecondary = DarkBrown,          // Dark text on secondary (light peach)
    onTertiary = DarkBrown,           // Dark text on tertiary (cream)
    onBackground = DarkBrown,         // Dark text on background
    onSurface = DarkBrown             // Dark text on surface
)

@Composable
fun IHostTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}