package alexis.rioc.proje_iot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.russhwolf.settings.Settings
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun HomeScreen() {
    val settings = Settings()
    val preferences = CalendarPreferences(settings)

    var currentMonth by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.month) }
    var currentYear by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.year) }

    val selectedPastDays = remember { mutableStateListOf<LocalDate>() }
    val selectedFutureDays = remember { mutableStateListOf<LocalDate>() }
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    // Chargement automatique des jours
    LaunchedEffect(Unit) {
        selectedPastDays.addAll(preferences.getPastDays().map { LocalDate.parse(it) })
        selectedFutureDays.addAll(preferences.getFutureDays().map { LocalDate.parse(it) })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                if (currentMonth == Month.JANUARY) {
                    currentMonth = Month.DECEMBER
                    currentYear--
                } else {
                    currentMonth = Month.entries.toTypedArray()[currentMonth.ordinal - 1]
                }
            }) {
                Text("Précédent")
            }

            Text(
                text = "${currentMonth.toFrench()} $currentYear",
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )



            Button(onClick = {
                if (currentMonth == Month.DECEMBER) {
                    currentMonth = Month.JANUARY
                    currentYear++
                } else {
                    currentMonth = Month.entries.toTypedArray()[currentMonth.ordinal + 1]
                }
            }) {
                Text("Suivant")
            }
        }


        CalendarGrid(
            currentMonth = currentMonth,
            currentYear = currentYear,
            today = today,
            selectedPastDays = selectedPastDays,
            selectedFutureDays = selectedFutureDays,
            preferences = preferences
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Jours réalisés : ${
                selectedPastDays.filter { it.month == currentMonth && it.year == currentYear }.size
            }, Jours à venir : ${
                selectedFutureDays.filter { it.month == currentMonth && it.year == currentYear }.size
            }",
            style = MaterialTheme.typography.body1
        )

    }
}

@Composable
fun CalendarGrid(
    currentMonth: Month,
    currentYear: Int,
    today: LocalDate,
    selectedPastDays: MutableList<LocalDate>,
    selectedFutureDays: MutableList<LocalDate>,
    preferences: CalendarPreferences
) {
    val firstDayOfMonth = LocalDate(currentYear, currentMonth, 1)
    val lastDayOfMonth = firstDayOfMonth.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        }

        var currentDay = firstDayOfMonth
        while (currentDay <= lastDayOfMonth) {
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { index ->
                    if (index < firstDayOfMonth.dayOfWeek.isoDayNumber % 7 && currentDay == firstDayOfMonth) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f)) // Cases vides avant le début du mois
                    } else if (currentDay <= lastDayOfMonth) {
                        Box(
                            modifier = Modifier.weight(1f).aspectRatio(1f), // Égalité de largeur
                            contentAlignment = Alignment.Center
                        ) {
                            DayCell(
                                day = currentDay,
                                today = today,
                                selectedPastDays = selectedPastDays,
                                selectedFutureDays = selectedFutureDays,
                                        preferences = preferences
                            )
                        }
                        currentDay = currentDay.plus(DatePeriod(days = 1))
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f)) // Cases vides après la fin du mois
                    }
                }

            }
        }
    }
}

@Composable
fun DayCell(
    day: LocalDate,
    today: LocalDate,
    selectedPastDays: MutableList<LocalDate>,
    selectedFutureDays: MutableList<LocalDate>,
    preferences: CalendarPreferences
) {
    val isSelectedPast = selectedPastDays.contains(day)
    val isSelectedFuture = selectedFutureDays.contains(day)
    val backgroundColor = when {
        isSelectedPast -> Color.Green
        isSelectedFuture -> Color.Yellow
        day == today -> ColorDarkBlue
        else -> Color.LightGray
    }

    Box(
        modifier = Modifier
            .fillMaxSize() // Remplit toute la cellule
            .background(backgroundColor, shape = RoundedCornerShape(10.dp))
            .clickable {
                if (day <= today) {
                    if (isSelectedPast) selectedPastDays.remove(day) else selectedPastDays.add(day)
                } else {
                    if (isSelectedFuture) selectedFutureDays.remove(day) else selectedFutureDays.add(day)
                }

                // Sauvegarde automatique après chaque clic
                preferences.saveDays(
                    selectedPastDays.map { it.toString() },
                    selectedFutureDays.map { it.toString() }
                )
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.dayOfMonth.toString(), fontSize = 16.sp)
    }

}

fun Month.toFrench(): String {
    val months = listOf(
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    )
    return months[this.ordinal]
}