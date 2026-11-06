package pe.edu.upc.safelab.shared.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.safelab.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentModuleTitle: String,
    language: String,
    isDarkTheme: Boolean,
    onBack: (() -> Unit)?,
    onLanguageToggle: () -> Unit,
    onThemeToggle: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_nav_back),
                        contentDescription = if (language == "ES") "Volver" else "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Box(
                    modifier = Modifier.padding(start = 12.dp).size(38.dp)
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.safelab_shield),
                        contentDescription = "SafeLab logo",
                        modifier = Modifier.size(31.dp)
                    )
                }
            }
        },
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SafeLab",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = currentModuleTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier.size(39.dp).semantics {
                    contentDescription = if (isDarkTheme) "Light theme" else "Dark theme"
                }
            ) {
                Text(
                    text = if (isDarkTheme) "☀" else "☾",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 22.sp
                )
            }
            Surface(
                modifier = Modifier.semantics {
                    contentDescription = if (language == "ES") "Switch to English" else "Cambiar a español"
                }.clickable(onClick = onLanguageToggle),
                color = Color(0xFF4338CA),
                shape = CircleShape
            ) {
                Text(
                    text = language,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 9.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )
            }
            Box(
                modifier = Modifier.padding(start = 7.dp, end = 10.dp).size(34.dp)
                    .background(Color(0xFF1056D9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("ML", color = Color.White, fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelMedium)
            }
        }
    )
}