package no.ntnu.prog2007.ihost.ui.screens.addevent

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddEventScreen(
    viewModel: EventViewModel,
    onEventCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_DATE)) }
    var eventTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Event tittel", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .padding(bottom = 16.dp),
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Beskrivelse (valgfritt)", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .heightIn(min = 80.dp),
            minLines = 3,
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        OutlinedTextField(
            value = eventDate,
            onValueChange = { eventDate = it },
            label = { Text("Dato (YYYY-MM-DD)", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        OutlinedTextField(
            value = eventTime,
            onValueChange = { eventTime = it },
            label = { Text("Tid (HH:mm) (valgfritt)", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Sted (valgfritt)", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !uiState.isLoading,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107),
                cursorColor = Color(0xFFFFC107)
            )
        )

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.createEvent(
                        title = title,
                        description = description.ifEmpty { null },
                        eventDate = eventDate,
                        eventTime = eventTime.ifEmpty { null },
                        location = location.ifEmpty { null }
                    )
                    onEventCreated()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                enabled = title.isNotEmpty() && eventDate.isNotEmpty() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Opprett event")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
