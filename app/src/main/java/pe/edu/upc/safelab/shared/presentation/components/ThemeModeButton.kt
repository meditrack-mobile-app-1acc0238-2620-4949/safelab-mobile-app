package pe.edu.upc.safelab.shared.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.R

/** The same outlined theme button on login, registration and internal screens. */
@Composable
fun ThemeModeButton(isDarkTheme: Boolean, language: String, onClick: () -> Unit) {
    val nextTheme = if (isDarkTheme) {
        if (language == "ES") "Activar modo claro" else "Switch to light theme"
    } else {
        if (language == "ES") "Activar modo oscuro" else "Switch to dark theme"
    }
    Box(
        modifier = Modifier.size(40.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(if (isDarkTheme) R.drawable.ic_theme_sun else R.drawable.ic_theme_moon),
            contentDescription = nextTheme,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )
    }
}
