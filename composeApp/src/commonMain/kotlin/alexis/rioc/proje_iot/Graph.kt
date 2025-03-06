package alexis.rioc.proje_iot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MyChart(
    bpm: List<Pair<Float, Float>>,
    temperature: List<Pair<Float, Float>>,
    speed: List<Pair<Float, Float>>,
    selectedOption: DateRangeOption,
) {
    if (bpm.isEmpty() || temperature.isEmpty() || speed.isEmpty()) return

    val maxY = maxOf(
        bpm.maxOf { it.second },
        temperature.maxOf { it.second },
        speed.maxOf { it.second }
    )

    val xAxisLabels = when (selectedOption) {
        DateRangeOption.Week -> 7
        DateRangeOption.Month -> 30
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.8f)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val stepX = width / xAxisLabels
            val stepY = height / maxY

            // Fonction pour dessiner une courbe
            fun drawLineSegments(data: List<Pair<Float, Float>>, color: Color) {
                if (data.isNotEmpty()) {
                    // Dessiner les segments de droite
                    data.windowed(2) { pair ->
                        val (start, end) = pair
                        val startX = start.first * stepX
                        val startY = height - start.second * stepY
                        val endX = end.first * stepX
                        val endY = height - end.second * stepY

                        drawLine(
                            color = color,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 4f // Appliquer l'épaisseur
                        )
                    }
                }
            }

            // Dessiner les lignes
            drawLineSegments(bpm, Color.Blue)
            drawLineSegments(temperature, Color.Red)
            drawLineSegments(speed, Color.Green)

            // Dessiner l'axe X
            for (i in 0 until xAxisLabels) {
                drawLine(
                    start = Offset(i * stepX, height),
                    end = Offset(i * stepX, height - 10f),
                    color = Color.Black,
                    strokeWidth = 2f // Définir l'épaisseur pour les axes si nécessaire
                )
            }

            // Dessiner l'axe Y
            for (i in 0..5) {
                val y = height - (i * (height / 5))
                drawLine(
                    start = Offset(0f, y),
                    end = Offset(10f, y),
                    color = Color.Black,
                    strokeWidth = 2f // Définir l'épaisseur pour les axes si nécessaire
                )
            }
        }
    }
}
