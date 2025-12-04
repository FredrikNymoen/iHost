package no.ntnu.prog2007.ihost.ui.screens.auth.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.ui.screens.auth.welcome.components.WelcomeLogo
import no.ntnu.prog2007.ihost.ui.screens.auth.welcome.components.WelcomeButtons

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WelcomeLogo()

        Spacer(modifier = Modifier.height(48.dp))

        WelcomeButtons(
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToSignUp = onNavigateToSignUp
        )
    }
}
