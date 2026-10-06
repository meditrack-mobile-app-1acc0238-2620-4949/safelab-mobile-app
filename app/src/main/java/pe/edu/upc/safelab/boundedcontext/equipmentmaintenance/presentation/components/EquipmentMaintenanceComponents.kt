package pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.ConditionLevel
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.MaintenanceStatus
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.ReliabilityLevel
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.StabilityPoint
import pe.edu.upc.safelab.ui.theme.SafeLabAccent
import pe.edu.upc.safelab.ui.theme.SafeLabBlue
import pe.edu.upc.safelab.ui.theme.SafeLabDanger
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

fun conditionColor(level: ConditionLevel): Color = when (level) {
    ConditionLevel.GOOD -> SafeLabSuccess
    ConditionLevel.ATTENTION -> SafeLabWarning
    ConditionLevel.CRITICAL -> SafeLabDanger
}

fun reliabilityColor(level: ReliabilityLevel): Color = when (level) {
    ReliabilityLevel.RELIABLE -> SafeLabSuccess
    ReliabilityLevel.MODERATE -> SafeLabWarning
    ReliabilityLevel.AT_RISK -> SafeLabDanger
}

fun maintenanceStatusColor(status: MaintenanceStatus): Color = when (status) {
    MaintenanceStatus.SCHEDULED -> SafeLabBlue
    MaintenanceStatus.IN_PROGRESS -> SafeLabWarning
    MaintenanceStatus.COMPLETED -> SafeLabSuccess
}

@Composable
fun EquipmentHeader(
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
fun EquipmentSectionCard(
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

@Composable
fun LevelChip(text: String, color: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1
    )
}

@Composable
fun OptionChips(
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

@Composable
fun DetailLine(label: String, value: String, valueColor: Color? = null) {
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
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = SafeLabPrimary,
    supportingText: String? = null
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
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Circular score (0 - 100). */
@Composable
fun ScoreRing(score: Int, color: Color, caption: String) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier.size(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(138.dp)) {
            val stroke = 16.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * (score.coerceIn(0, 100) / 100f),
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/** Horizontal bar (usage, load, uptime). */
@Composable
fun ProgressLine(label: String, valueText: String, fraction: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DetailLine(label = label, value = valueText)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(color)
            )
        }
    }
}

/** Weekly stability bars: % of time inside the allowed range. */
@Composable
fun StabilityChart(points: List<StabilityPoint>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { point ->
            val color = when {
                point.percentInRange >= 95f -> SafeLabSuccess
                point.percentInRange >= 85f -> SafeLabWarning
                else -> SafeLabDanger
            }
            // Bars start at 50 % so small differences remain visible.
            val fraction = ((point.percentInRange - 50f) / 50f).coerceIn(0.05f, 1f)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = "${point.percentInRange.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(fraction * 0.75f)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(color)
                )
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SubmitButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SafeLabPrimary)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun ResultMessage(message: String?, isError: Boolean) {
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

@Composable
fun NoDataState(title: String, message: String) {
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

@Composable
fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
