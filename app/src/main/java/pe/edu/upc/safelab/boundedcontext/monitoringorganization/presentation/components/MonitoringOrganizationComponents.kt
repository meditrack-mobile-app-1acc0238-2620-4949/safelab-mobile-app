package pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.EquipmentStatus
import pe.edu.upc.safelab.ui.theme.SafeLabAccent
import pe.edu.upc.safelab.ui.theme.SafeLabDanger
import pe.edu.upc.safelab.ui.theme.SafeLabMuted
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

fun equipmentStatusColor(status: EquipmentStatus): Color = when (status) {
    EquipmentStatus.COMPLIANT -> SafeLabSuccess
    EquipmentStatus.WARNING -> SafeLabWarning
    EquipmentStatus.CRITICAL -> SafeLabDanger
    EquipmentStatus.NOT_MONITORED -> SafeLabMuted
}

/** Gradient header used at the top of each screen of the module. */
@Composable
fun OrganizationHeader(
    eyebrow: String,
    title: String,
    description: String,
    onBack: (() -> Unit)? = null,
    backLabel: String = "Back"
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (onBack != null) {
            OutlinedButton(onClick = onBack) {
                Text("← $backLabel", fontWeight = FontWeight.Bold)
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                SafeLabAccent.copy(alpha = 0.12f),
                                SafeLabPrimary.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = eyebrow.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun OrganizationSectionCard(
    title: String,
    eyebrow: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

/** Small counter tile (sites, areas, equipment...). */
@Composable
fun SummaryTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = SafeLabPrimary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.4.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = accent,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/** Status label: color + text, so the state never depends on color alone. */
@Composable
fun StatusChip(text: String, color: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold
    )
}

/** Horizontal list of selectable options (filters and form choices). */
@Composable
fun ChoiceChips(
    options: List<Pair<String?, String>>,
    selected: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Text(
                text = label,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable { onSelected(value) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/** Label / value line used inside cards. */
@Composable
fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(start = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Clickable list card with a leading symbol, title, subtitle and trailing status. */
@Composable
fun OrganizationListCard(
    symbol: String,
    title: String,
    subtitle: String,
    statusText: String?,
    statusColor: Color,
    onClick: (() -> Unit)?,
    footer: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(statusColor.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = symbol,
                        color = statusColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (statusText != null) {
                    StatusChip(text = statusText, color = statusColor)
                }
            }
            if (footer != null) {
                footer()
            }
        }
    }
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SafeLabPrimary)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

/** Success or validation message shown under a form. */
@Composable
fun FormMessage(message: String?, isError: Boolean) {
    if (message == null) return
    val color = if (isError) SafeLabDanger else SafeLabSuccess
    Text(
        text = (if (isError) "△ " else "✓ ") + message,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(12.dp),
        color = color,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold
    )
}

/** Empty state required by the acceptance criteria ("no items available"). */
@Composable
fun EmptyState(title: String, message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "○", fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
