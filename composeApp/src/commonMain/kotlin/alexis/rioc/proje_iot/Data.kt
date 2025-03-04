package alexis.rioc.proje_iot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import at.maximilianproell.multiplatformchart.common.AxisConfigDefaults
import at.maximilianproell.multiplatformchart.linechart.LineChart
import at.maximilianproell.multiplatformchart.linechart.config.LineConfigDefaults
import at.maximilianproell.multiplatformchart.linechart.model.DataPoint
import at.maximilianproell.multiplatformchart.linechart.model.LineDataSet
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

enum class DateRangeOption {
    Month, Week
}

@Composable
fun DataScreen() {
    var selectedOption by remember { mutableStateOf(DateRangeOption.Month) }
    var currentDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) }
    var filteredData by remember { mutableStateOf(generateData(selectedOption, currentDate)) }

    Column(
        modifier = Modifier.fillMaxHeight(0.8f),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Activité", style = MaterialTheme.typography.h6)

        TabSelector(selectedOption) { option ->
            selectedOption = option
            currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            filteredData = generateData(selectedOption, currentDate) // Met à jour les données
        }

        NavigationButtons(selectedOption, currentDate, onDateChange = { newDate ->
            currentDate = newDate
            filteredData = generateData(selectedOption, currentDate) // Met à jour les données
        })

        MyChart(filteredData, selectedOption)
    }
}

fun generateData(option: DateRangeOption, date: kotlinx.datetime.LocalDateTime): List<Pair<Float, Float>> {
    return when (option) {
        DateRangeOption.Week -> {
            List(7) { index -> (index + 1).toFloat() to Random.nextFloat() * 10 }
        }
        DateRangeOption.Month -> {
            List(30) { index -> (index + 1).toFloat() to Random.nextFloat() * 10 }
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

@Composable
fun NavigationButtons(
    selectedOption: DateRangeOption,
    currentDate: kotlinx.datetime.LocalDateTime,
    onDateChange: (kotlinx.datetime.LocalDateTime) -> Unit
) {
    val minDate = kotlinx.datetime.LocalDateTime(2024, 1, 1, 0, 0)
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
fun MyChart(data: List<Pair<Float, Float>>, selectedOption: DateRangeOption) {
    if (data.isEmpty()) return

    val maxX = data.maxOf { it.first }
    val maxY = data.maxOf { it.second }

    // Configuration de l'axe X en fonction de l'option choisie
    val xAxisLabels = when (selectedOption) {
        DateRangeOption.Week -> 7 // 7 jours pour une semaine
        DateRangeOption.Month -> 30 // 30 jours pour un mois
    }

    LineChart(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.8f)
            .padding(5.dp),
        lineDataSets = listOf(LineDataSet("Données", data.map { DataPoint(it.first, it.second) }, ColorDarkBlue)),
        maxVisibleYValue = maxY + 5f,
        xAxisConfig = AxisConfigDefaults.xAxisConfigDefaults().copy(
            axisColor = Color.Black,
            allowBorderTextClipping = false,
            numberOfLabels = xAxisLabels // Nombre de labels adaptés
        ),
        yAxisConfig = AxisConfigDefaults.yAxisConfigDefaults().copy(numberOfLabels = 6),
        lineConfig = LineConfigDefaults.lineConfigDefaults().copy(showLineDots = false)
    )
}