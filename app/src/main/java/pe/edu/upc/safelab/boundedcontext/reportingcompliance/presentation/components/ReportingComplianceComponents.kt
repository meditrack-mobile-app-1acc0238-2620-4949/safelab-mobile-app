package pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.safelab.ui.theme.SafeLabDanger
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

enum class ReportingSection(val label: String) {
    REPORTS("Reports"),
    GENERATE("Generate"),
    HISTORY("History"),
    COMPLIANCE("Compliance"),
    EXPORT("Export")
}

@Composable
fun ReportingSectionNavigation(
    selectedSection: ReportingSection,
    onSectionSelected: (ReportingSection) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ReportingSection.entries) { section ->
            val selected = section == selectedSection
            Surface(
                modifier = Modifier.clickable { onSectionSelected(section) },
                color = if (selected) SafeLabPrimary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = if (selected) null else BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                )
            ) {
                Text(
                    text = section.label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ReportingScreenHeader(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (actionLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = SafeLabPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ReportingMetricCard(
    label: String,
    value: String,
    detail: String,
    marker: String,
    markerColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(markerColor.copy(alpha = 0.13f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = marker,
                    color = markerColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = detail,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ReportingTag(
    text: String,
    type: String = "neutral"
) {
    val background = when (type.lowercase()) {
        "success", "ready", "complies", "active" -> SafeLabSuccess.copy(alpha = 0.13f)
        "warning", "draft", "paused" -> SafeLabWarning.copy(alpha = 0.16f)
        "danger", "violation", "critical" -> SafeLabDanger.copy(alpha = 0.13f)
        "primary" -> SafeLabPrimary.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val foreground = when (type.lowercase()) {
        "success", "ready", "complies", "active" -> Color(0xFF047857)
        "warning", "draft", "paused" -> Color(0xFFB45309)
        "danger", "violation", "critical" -> Color(0xFFB91C1C)
        "primary" -> SafeLabPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = background,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = foreground,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ReportingChoiceRow(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            val isSelected = option == selected
            Surface(
                modifier = Modifier.clickable { onSelected(option) },
                color = if (isSelected) SafeLabPrimary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(999.dp),
                border = if (isSelected) null else BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                )
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ReportingSectionTitle(
    title: String,
    subtitle: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
