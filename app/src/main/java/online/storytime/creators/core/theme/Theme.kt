package online.storytime.creators.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object STColor {
    val background = Color(0xFF000000)
    val surface = Color(0xFF0E0E0E)
    val surfaceElevated = Color(0xFF171717)
    val border = Color(0x1FFFFFFF)
    val primary = Color(0xFFF97316)
    val accent = Color(0xFFFFCB67)
    val brandDeep = Color(0xFFFF7A00)
    val textPrimary = Color(0xFFF5F5F7)
    val textSecondary = Color(0xFF9EA3AE)
    val textMuted = Color(0xFF737782)
    val danger = Color(0xFFF0544F)
    val success = Color(0xFF4CC878)

    val brandGradient: Brush
        get() = Brush.horizontalGradient(
            listOf(
                Color(0xFFFFCB67),
                Color(0xFFFF9F1C),
                Color(0xFFFF7A00),
                Color(0xFFFFB347),
            )
        )

    val heroGradient: Brush
        get() = Brush.linearGradient(
            listOf(primary.copy(alpha = 0.30f), surface)
        )
}

private val StoryTimeColorScheme = darkColorScheme(
    primary = STColor.primary,
    onPrimary = Color.Black,
    secondary = STColor.accent,
    background = STColor.background,
    onBackground = STColor.textPrimary,
    surface = STColor.surface,
    onSurface = STColor.textPrimary,
    surfaceVariant = STColor.surfaceElevated,
    error = STColor.danger,
)

@Composable
fun StoryTimeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StoryTimeColorScheme,
        content = content,
    )
}
