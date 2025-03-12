package alexis.rioc.proje_iot

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ColorDarkBlue = Color(0xFF0D47A1)

private val AppColorPalette = lightColors(
    primary = ColorDarkBlue, // Bleu foncé
    primaryVariant = Color(0xFF1565C0), // Bleu un peu plus clair pour interactions
    secondary = Color(0xFF64B5F6), // Bleu clair pour éléments secondaires
    background = Color.White, // Fond blanc
    surface = Color.White, // Surface blanche
    onPrimary = Color.White, // Texte sur bleu foncé (icône sélectionnée)
    onSecondary = Color.Gray, // Icônes non sélectionnées
    onBackground = Color.Black, // Texte sur fond blanc
    onSurface = Color.Black // Texte sur surface blanche
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = AppColorPalette,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
