package alexis.rioc.proje_iot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import androidx.compose.runtime.LaunchedEffect
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

enum class DateRangeOption {
    Month, Week
}

@Composable
fun DataScreen() {
    var selectedOption by remember { mutableStateOf(DateRangeOption.Month) }
    var currentDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) }

    // Crée des variables d'état pour stocker les données générées
    var bpmData by remember { mutableStateOf(emptyList<Pair<Float, Float>>()) }
    var temperatureData by remember { mutableStateOf(emptyList<Pair<Float, Float>>()) }
    var speedData by remember { mutableStateOf(emptyList<Pair<Float, Float>>()) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    LaunchedEffect(selectedOption, currentDate) {
        val result = fetchData(SessionManager.userLogin, currentDate, selectedOption)
        if (result.isNotEmpty()) {
            bpmData = result.mapIndexed { index, data -> index.toFloat() to data.bpm.toFloat() }
            temperatureData = result.mapIndexed { index, data -> index.toFloat() to data.temperature.toFloat() }
            speedData = result.mapIndexed { index, data -> index.toFloat() to data.vitesse.toFloat() }
        }
    }


    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Activité", style = MaterialTheme.typography.h6, modifier = Modifier.padding(top = 5.dp))

        // Sélection de l'option de date (semaine/mois)
        TabSelector(selectedOption) { option ->
            selectedOption = option
        }

        // Navigation des dates
        NavigationButtons(selectedOption, currentDate) { newDate ->
            currentDate = newDate
        }

        // Affiche le graphique avec les données mises à jour
        MyChart(bpmData, temperatureData, speedData, selectedOption)

        if (showDialog) {
            ShowAlertDialog(
                message = dialogMessage,
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun NavigationButtons(
    selectedOption: DateRangeOption,
    currentDate: LocalDateTime,
    onDateChange: (LocalDateTime) -> Unit
) {
    val minDate = LocalDateTime(2024, 1, 1, 0, 0)
    val maxDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            ),
            onClick = {
                val newDate = when (selectedOption) {
                    DateRangeOption.Month -> currentDate.date.minus(kotlinx.datetime.DatePeriod(months = 1)).atTime(currentDate.time)
                    DateRangeOption.Week -> currentDate.date.minus(kotlinx.datetime.DatePeriod(days = 7)).atTime(currentDate.time)
                }
                if (newDate >= minDate) {
                    onDateChange(newDate)
                }
            },
            enabled = when (selectedOption) {
                DateRangeOption.Month -> currentDate.date.minus(kotlinx.datetime.DatePeriod(months = 1)).atTime(currentDate.time) >= minDate
                DateRangeOption.Week -> currentDate.date.minus(kotlinx.datetime.DatePeriod(days = 7)).atTime(currentDate.time) >= minDate
            }
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Précédent")
        }

        Text(
            text = when (selectedOption) {
                DateRangeOption.Month -> "${currentDate.month.toFrench()} ${currentDate.year}"
                DateRangeOption.Week -> {
                    val startOfWeek = currentDate.date.minus(kotlinx.datetime.DatePeriod(days = currentDate.date.dayOfWeek.ordinal))
                    val endOfWeek = startOfWeek.plus(kotlinx.datetime.DatePeriod(days = 6))

                    "${startOfWeek.dayOfMonth} ${startOfWeek.month.toFrench()} - ${endOfWeek.dayOfMonth} ${endOfWeek.month.toFrench()} ${currentDate.year}"
                }
            },
            style = MaterialTheme.typography.body1
        )

        Button(
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            ),
            onClick = {
                val newDate = when (selectedOption) {
                    DateRangeOption.Month -> currentDate.date.plus(kotlinx.datetime.DatePeriod(months = 1)).atTime(currentDate.time)
                    DateRangeOption.Week -> currentDate.date.plus(kotlinx.datetime.DatePeriod(days = 7)).atTime(currentDate.time)
                }
                if (newDate <= maxDate) {
                    onDateChange(newDate)
                }
            },
            enabled = when (selectedOption) {
                DateRangeOption.Month -> currentDate.date.plus(kotlinx.datetime.DatePeriod(months = 1)).atTime(currentDate.time) <= maxDate
                DateRangeOption.Week -> currentDate.date.plus(kotlinx.datetime.DatePeriod(days = 7)).atTime(currentDate.time) <= maxDate
            }
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Suivant")
        }
    }
}


@Composable
fun TabSelector(selectedOption: DateRangeOption, onOptionSelected: (DateRangeOption) -> Unit) {
    TabRow(
        selectedTabIndex = if (selectedOption == DateRangeOption.Week) 0 else 1,
        backgroundColor = Color.White,
        modifier = Modifier.height(30.dp),
        indicator = { /* Indicateur vide pour enlever la ligne sous l'onglet choisi */ }
    ) {
        Tab(
            selected = selectedOption == DateRangeOption.Week,
            onClick = { onOptionSelected(DateRangeOption.Week) },
            modifier = Modifier.background(ColorDarkBlue)
        ) {
            Text("Semaine", color = if (selectedOption == DateRangeOption.Week) Color.White else Color.Black)
        }
        Tab(
            selected = selectedOption == DateRangeOption.Month,
            onClick = { onOptionSelected(DateRangeOption.Month) },
            modifier = Modifier.background(ColorDarkBlue)
        ) {
            Text("Mois", color = if (selectedOption == DateRangeOption.Month) Color.White else Color.Black)
        }
    }
}

suspend fun fetchData(userLogin: String, chosenDate: LocalDateTime, selectedOption: DateRangeOption): List<DataModel> {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    try {
        val response: HttpResponse = client.get("http://$IP_MACHINE:8080/data") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            parameter("user", userLogin)
        }

        if (response.status == HttpStatusCode.OK) {
            val result = response.body<List<DataModel>>()
            val timeZone = TimeZone.currentSystemDefault()
            val (startDate, endDate) = getDateRange(selectedOption, chosenDate)

            return result.filter {
                val instant = Instant.fromEpochSeconds(it.time)
                val date = instant.toLocalDateTime(timeZone).date
                date in startDate..endDate
            }
        }
    } catch (e: Exception) {
        println("Erreur: $e")
    }
    return emptyList()
}

fun getDateRange(selectedOption: DateRangeOption, chosenDate: LocalDateTime): Pair<kotlinx.datetime.LocalDate, kotlinx.datetime.LocalDate> {
    return when (selectedOption) {
        DateRangeOption.Month -> {
            val firstDay = chosenDate.date.minus(kotlinx.datetime.DatePeriod(days = chosenDate.date.dayOfMonth - 1))
            val lastDay = firstDay.plus(kotlinx.datetime.DatePeriod(months = 1)).minus(kotlinx.datetime.DatePeriod(days = 1))
            Pair(firstDay, lastDay)
        }
        DateRangeOption.Week -> {
            val firstDay = chosenDate.date.minus(kotlinx.datetime.DatePeriod(days = chosenDate.date.dayOfWeek.ordinal))
            val lastDay = firstDay.plus(kotlinx.datetime.DatePeriod(days = 6))
            Pair(firstDay, lastDay)
        }
    }
}


@Composable
fun ShowAlertDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Message")
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}