package pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Duration
import java.time.Instant
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.BatchStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.CalibrationStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.ReadingStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorReading
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.ThresholdRule
import pe.edu.upc.safelab.ui.theme.SafeLabAccent
import pe.edu.upc.safelab.ui.theme.SafeLabBlue
import pe.edu.upc.safelab.ui.theme.SafeLabDanger
import pe.edu.upc.safelab.ui.theme.SafeLabMuted
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

// ---------- Status colors ----------

fun sensorStatusColor(status: SensorStatus): Color = when (status) {
    SensorStatus.ACTIVE -> SafeLabSuccess
    SensorStatus.DISCONNECTED -> SafeLabDanger
    SensorStatus.INACTIVE, SensorStatus.MAINTENANCE -> SafeLabMuted
}

fun readingStatusColor(status: ReadingStatus): Color = when (status) {
    ReadingStatus.NORMAL -> SafeLabSuccess
    ReadingStatus.OUT_OF_RANGE -> SafeLabWarning
    ReadingStatus.INVALID -> SafeLabDanger
}

fun calibrationStatusColor(status: CalibrationStatus): Color = when (status) {
    CalibrationStatus.COMPLETED -> SafeLabSuccess
    CalibrationStatus.PENDING, CalibrationStatus.OVERDUE -> SafeLabWarning
}

fun batchStatusColor(status: BatchStatus): Color = when (status) {
    BatchStatus.RECEIVED -> SafeLabBlue
    BatchStatus.PROCESSED -> SafeLabSuccess
    BatchStatus.REJECTED -> SafeLabDanger
}

// ---------- Formatting ----------

/** "2026-06-19T02:25:00Z" -> "2026-06-19 02:25". Date-only values are returned as they are. */
fun formatTimestamp(iso: String): String = iso.replace("T", " ").removeSuffix("Z").take(16)

/** Time elapsed since [from] as a short label ("25m ago", "2h 05m ago", "3d ago"). */
fun elapsedLabel(from: String, now: Instant): String {
    val minutes = runCatching { Duration.between(Instant.parse(from), now).toMinutes() }.getOrNull()
        ?: return "—"
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 24 * 60 -> "${minutes / 60}h ${(minutes % 60).toString().padStart(2, '0')}m ago"
        else -> "${minutes / (24 * 60)}d ago"
    }
}

// ---------- Layout ----------

@Composable
fun SensorHeader(
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
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
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
fun SensorSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
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

// ---------- Chips and inputs ----------

@Composable
fun StatusChip(text: String, color: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1
    )
}

/** Option of a filter row; [value] is null for the "All" option. */
data class FilterOption(
    val value: String?,
    val label: String,
    val color: Color = SafeLabPrimary
)

@Composable
fun FilterChips(
    options: List<FilterOption>,
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
        options.forEach { option ->
            val isSelected = option.value == selected
            Text(
                text = option.label,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(option.color.copy(alpha = 0.12f))
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) option.color else Color.Transparent,
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable { onSelected(option.value) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                color = option.color,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

// ---------- Data display ----------

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
            textAlign = TextAlign.End,
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
                letterSpacing = 0.4.sp,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = accent,
                fontWeight = FontWeight.Black,
                maxLines = 1
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
            Text(
                text = "○",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
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

// ---------- Charts ----------

/**
 * Position of a value over its threshold. The green zone is the allowed range and the marker
 * is the value; it turns [color] when the value is outside the zone.
 * [position] comes from [ThresholdRule.position].
 */
@Composable
fun ThresholdBar(position: Float, color: Color, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val ring = MaterialTheme.colorScheme.surface
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        val barHeight = 10.dp.toPx()
        val top = (size.height - barHeight) / 2
        val radius = CornerRadius(barHeight / 2, barHeight / 2)

        drawRoundRect(track, Offset(0f, top), Size(size.width, barHeight), radius)
        drawRoundRect(
            color = SafeLabSuccess.copy(alpha = 0.35f),
            topLeft = Offset(size.width * 0.25f, top),
            size = Size(size.width * 0.5f, barHeight),
            cornerRadius = radius
        )

        val center = Offset(size.width * position.coerceIn(0f, 1f), size.height / 2)
        drawCircle(ring, radius = 12.dp.toPx(), center = center)
        drawCircle(color, radius = 9.dp.toPx(), center = center)
    }
}

/**
 * Line chart of the readings of a sensor, oldest to newest, with the allowed range shaded.
 * Invalid readings are not drawn; out of range readings are marked.
 */
@Composable
fun ReadingsLineChart(
    readings: List<SensorReading>,
    threshold: ThresholdRule,
    modifier: Modifier = Modifier
) {
    val points = readings
        .filter { it.status != ReadingStatus.INVALID }
        .sortedBy { it.recordedAt }
    if (points.isEmpty()) return

    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            val padding = 12.dp.toPx()
            val width = size.width - padding * 2
            val height = size.height - padding * 2

            val low = minOf(threshold.minValue, points.minOf { it.value })
            val high = maxOf(threshold.maxValue, points.maxOf { it.value })
            val margin = (high - low).takeIf { it > 0.0 }?.times(0.1) ?: 1.0
            val from = low - margin
            val span = (high + margin) - from

            fun x(index: Int): Float =
                if (points.size == 1) padding + width / 2 else padding + width * index / (points.size - 1)

            fun y(value: Double): Float = padding + height * (1f - ((value - from) / span).toFloat())

            // Allowed range band.
            val bandTop = y(threshold.maxValue)
            val bandBottom = y(threshold.minValue)
            drawRoundRect(
                color = SafeLabSuccess.copy(alpha = 0.12f),
                topLeft = Offset(padding, bandTop),
                size = Size(width, bandBottom - bandTop),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
            drawLine(gridColor, Offset(padding, bandTop), Offset(padding + width, bandTop), strokeWidth = 1.dp.toPx())
            drawLine(gridColor, Offset(padding, bandBottom), Offset(padding + width, bandBottom), strokeWidth = 1.dp.toPx())

            // Readings line.
            val path = Path()
            points.forEachIndexed { index, reading ->
                if (index == 0) path.moveTo(x(index), y(reading.value)) else path.lineTo(x(index), y(reading.value))
            }
            drawPath(
                path = path,
                color = SafeLabPrimary,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Points: out of range readings are highlighted and the latest one is larger.
            points.forEachIndexed { index, reading ->
                val isLast = index == points.lastIndex
                if (reading.status == ReadingStatus.OUT_OF_RANGE) {
                    drawCircle(SafeLabWarning, radius = 5.dp.toPx(), center = Offset(x(index), y(reading.value)))
                } else if (isLast) {
                    drawCircle(SafeLabPrimary, radius = 5.dp.toPx(), center = Offset(x(index), y(reading.value)))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTimestamp(points.first().recordedAt).takeLast(5),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatTimestamp(points.last().recordedAt).takeLast(5),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
