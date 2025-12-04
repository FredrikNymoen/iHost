package no.ntnu.prog2007.ihost.ui.screens.auth.signup.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PasswordMismatchError(
    show: Boolean,
    modifier: Modifier = Modifier
) {
    if (show) {
        Text(
            text = "Passwords do not match",
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp)
        )
    }
}
