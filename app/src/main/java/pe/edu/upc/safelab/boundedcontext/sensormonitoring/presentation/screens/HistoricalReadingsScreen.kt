package pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.data.repository.SensorMonitoringRepository
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.ReadingStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.Sensor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorReading
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorType
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.FilterChips
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.FilterOption
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.MetricTile
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.ReadingsLineChart
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.SensorHeader
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.SensorSectionCard
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.StatusChip
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.formatTimestamp
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.readingStatusColor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.targetRangeLabel
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabTheme
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

/**
 * User stories covered:
 * - Consult the historical readings of a sensor, newest first, filtered by reading status.
 * - See the minimum, maximum and average value and how many readings were out of range.
 * - Follow the trend of the readings over the allowed range of the sensor.
 */
@Composable
fun HistoricalReadingsScreen(
    sensorId: String?,
    onBack: () -> Unit = {}
) {
    val repository = SensorMonitoringRepository
    val sensor = repository.findSensor(sensorId)
    var statusFilter by rememberSaveable { mutableStateOf<String?>(null) }

    val all = repository.readingsOf(sensorId)
    val visible = repository.readingsOf(
        sensorId = sensorId,
        status = ReadingStatus.entries.firstOrNull { it.name == statusFilter }
    )
    val stats = repository.readingStats(sensorId)
    val filterOptions = listOf(FilterOption(null, "All (${all.size})")) +
        ReadingStatus.entries.map { status ->
            FilterOption(
                value = status.name,
                label = "${status.label} (${all.count { it.status == status }})",
                color = readingStatusColor(status)
            )
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SensorHeader(
                eyebrow = sensor?.code ?: "Sensor monitoring",
                title = "Historical readings",
                description = if (sensor != null) {
                    "${sensor.name}. Allowed range: ${targetRangeLabel(sensor)}."
                } else {
                    "Select a sensor to see its historical readings."
                },
                onBack = onBack,
                backLabel = "Back"
            )
        }

        if (sensor == null) {
            item {
                NoDataState(
                    title = "Sensor not found",
                    message = "The selected sensor does not exist. Go back and choose another one."
                )
            }
            return@LazyColumn
        }

        if (stats != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricTile(
                            label = "Minimum",
                            value = sensor.type.formatValue(stats.min, sensor.unit),
                            modifier = Modifier.weight(1f),
                            accent = SafeLabPrimary
                        )
                        MetricTile(
                            label = "Maximum",
                            value = sensor.type.formatValue(stats.max, sensor.unit),
                            modifier = Modifier.weight(1f),
                            accent = SafeLabPrimary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricTile(
                            label = "Average",
                            value = averageLabel(sensor, stats.average),
                            modifier = Modifier.weight(1f),
                            accent = SafeLabSuccess
                        )
                        MetricTile(
                            label = "Out of range",
                            value = stats.outOfRangeCount.toString(),
                            modifier = Modifier.weight(1f),
                            accent = if (stats.outOfRangeCount > 0) SafeLabWarning else SafeLabSuccess,
                            supportingText = "of ${stats.count} readings"
                        )
                    }
                }
            }
        }

        item {
            SensorSectionCard(title = "Trend", eyebrow = "Last ${all.size} readings") {
                if (all.none { it.status != ReadingStatus.INVALID }) {
                    Text(
                        text = "There are no valid readings to draw.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ReadingsLineChart(readings = all, threshold = sensor.threshold)
                    Text(
                        text = "The shaded band is the allowed range. Orange points are out of range; invalid readings are not drawn.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            FilterChips(options = filterOptions, selected = statusFilter, onSelected = { statusFilter = it })
        }

        if (visible.isEmpty()) {
            item {
                NoDataState(
                    title = "No readings found",
                    message = "This sensor has no readings with the selected status."
                )
            }
        } else {
            items(visible, key = { it.id }) { reading -> ReadingRow(reading) }
        }
    }
}

/** Doors only have two values, so their average is shown as the share of time they were open. */
private fun averageLabel(sensor: Sensor, average: Double): String =
    if (sensor.type == SensorType.DOOR_STATUS) {
        "${(average * 100).roundToInt()}% open"
    } else {
        sensor.type.formatValue(average, sensor.unit)
    }

@Composable
private fun ReadingRow(reading: SensorReading) {
    val color = readingStatusColor(reading.status)
    val highlighted = reading.status != ReadingStatus.NORMAL

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (highlighted) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reading.displayValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (highlighted) color else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatTimestamp(reading.recordedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(text = reading.status.label, color = color)
        }
    }
}

@Preview(showBackground = true, name = "Historical readings - light")
@Composable
private fun HistoricalReadingsScreenLightPreview() {
    SafeLabTheme(darkTheme = false) { HistoricalReadingsScreen(sensorId = "sen-003") }
}

@Preview(showBackground = true, name = "Historical readings - dark")
@Composable
private fun HistoricalReadingsScreenDarkPreview() {
    SafeLabTheme(darkTheme = true) { HistoricalReadingsScreen(sensorId = "sen-003") }
}
