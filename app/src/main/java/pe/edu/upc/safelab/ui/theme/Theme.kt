package pe.edu.upc.safelab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SafeLabPrimary,
    onPrimary = SafeLabSurface,
    secondary = SafeLabBlue,
    tertiary = SafeLabAccent,
    error = SafeLabDanger,
    background = SafeLabBackground,
    onBackground = SafeLabText,
    surface = SafeLabSurface,
    onSurface = SafeLabText,
    surfaceVariant = SafeLabSurfaceSoft,
    onSurfaceVariant = SafeLabMuted
)

private val DarkColorScheme = darkColorScheme(
    primary = SafeLabPrimary,
    onPrimary = SafeLabDarkText,
    secondary = SafeLabBlue,
    tertiary = SafeLabAccent,
    error = SafeLabDanger,
    background = SafeLabDarkBackground,
    onBackground = SafeLabDarkText,
    surface = SafeLabDarkSurface,
    onSurface = SafeLabDarkText,
    surfaceVariant = SafeLabDarkSurfaceSoft,
    onSurfaceVariant = SafeLabDarkMuted
)

@Composable
fun SafeLabTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
