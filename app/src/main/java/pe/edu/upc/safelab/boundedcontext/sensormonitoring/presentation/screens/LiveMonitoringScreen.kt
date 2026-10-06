package pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.data.repository.SensorMonitoringRepository
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.ReadingStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.Sensor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorType
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.DetailLine
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.FilterChips
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.FilterOption
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.MetricTile
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.SearchField
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.SensorHeader
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.StatusChip
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.connectionLabel
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.elapsedLabel
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.formatTimestamp
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.readingStatusColor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.sensorStatusColor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.targetRangeLabel
import pe.edu.upc.safelab.ui.theme.SafeLabDanger
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabTheme
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

/**
 * User stories covered:
 * - View the live readings of the monitored sensors.
 * - Search sensors by name, code or responsible and filter them by type and reading status.
 * - Identify sensors with readings out of range or invalid, and sensors that are offline.
 */
@Composable
fun LiveMonitoringScreen(
    onOpenDetail: (String) -> Unit = {},
    onOpenOffline: () -> Unit = {}
) {
    val repository = SensorMonitoringRepository
    var query by rememberSaveable { mutableStateOf("") }
    var typeFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var statusFilter by rememberSaveable { mutableStateOf<String?>(null) }

    val all = repository.sensors()
    val summary = repository.summary()
    val visible = repository.sensors(
        query = query,
        type = SensorType.entries.firstOrNull { it.name == typeFilter },
        readingStatus = ReadingStatus.entries.firstOrNull { it.name == statusFilter }
    )
    val hasFilters = query.isNotBlank() || typeFilter != null || statusFilter != null

    val typeOptions = listOf(FilterOption(null, "All types (${all.size})")) +
        SensorType.entries.map { type ->
            FilterOption(type.name, "${type.label} (${all.count { it.type == type }})")
        }
    val statusOptions = listOf(FilterOption(null, "All readings (${all.size})")) +
        ReadingStatus.entries.map { status ->
            FilterOption(
                value = status.name,
                label = "${status.label} (${all.count { it.readingStatus == status }})",
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
                eyebrow = "Sensor monitoring",
                title = "Live monitoring",
                description = "Latest reading of every sensor against its allowed range. Sensors with problems are highlighted."
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile(
                        label = "Total",
                        value = summary.total.toString(),
                        modifier = Modifier.weight(1f),
                        accent = SafeLabPrimary
                    )
                    MetricTile(
                        label = "Normal",
                        value = summary.normal.toString(),
                        modifier = Modifier.weight(1f),
                        accent = SafeLabSuccess
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile(
                        label = "Out of range",
                        value = summary.outOfRange.toString(),
                        modifier = Modifier.weight(1f),
                        accent = SafeLabWarning
                    )
                    MetricTile(
                        label = "Offline",
                        value = summary.offline.toString(),
                        modifier = Modifier.weight(1f),
                        accent = SafeLabDanger
                    )
                }
            }
        }

        item {
            OutlinedButton(onClick = onOpenOffline, modifier = Modifier.fillMaxWidth()) {
                Text("Offline sensors (${summary.offline})", fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }

        item {
            SearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Sensor, code or responsible"
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChips(options = typeOptions, selected = typeFilter, onSelected = { typeFilter = it })
                FilterChips(options = statusOptions, selected = statusFilter, onSelected = { statusFilter = it })
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    query = ""
                    typeFilter = null
                    statusFilter = null
                },
                enabled = hasFilters,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear filters", fontWeight = FontWeight.Bold)
            }
        }

        if (visible.isEmpty()) {
            item {
                NoDataState(
                    title = "No sensors found",
                    message = "No sensor matches the search and filters. Clear the filters to see all sensors."
                )
            }
        } else {
            items(visible, key = { it.id }) { sensor ->
                SensorCard(sensor = sensor, onClick = { onOpenDetail(sensor.id) })
            }
        }
    }
}

@Composable
private fun SensorCard(sensor: Sensor, onClick: () -> Unit) {
    val readingColor = readingStatusColor(sensor.readingStatus)
    val statusColor = sensorStatusColor(sensor.status)
    // The accent follows the most serious problem: a sensor that is not sending data, or a bad reading.
    val accent = when {
        sensor.status == SensorStatus.DISCONNECTED -> statusColor
        sensor.readingStatus != ReadingStatus.NORMAL -> readingColor
        else -> null
    }
    val shape = RoundedCornerShape(22.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (accent != null) Modifier.border(1.5.dp, accent, shape) else Modifier)
            .clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sensor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = sensor.code,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatusChip(text = sensor.readingStatus.label, color = readingColor)
                    if (sensor.status != SensorStatus.ACTIVE) {
                        StatusChip(text = sensor.status.label, color = statusColor)
                    }
                }
            }

            Text(
                text = sensor.latestReading.displayValue,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = readingColor
            )

            DetailLine("Target range", targetRangeLabel(sensor))
            DetailLine("Connection", connectionLabel(sensor), valueColor = statusColor)
            DetailLine("Responsible", sensor.responsible)
            DetailLine(
                label = "Last reading",
                value = "${formatTimestamp(sensor.latestReading.recordedAt)} · " +
                    elapsedLabel(sensor.latestReading.recordedAt, SensorMonitoringRepository.currentTime())
            )
        }
    }
}

@Preview(showBackground = true, name = "Live monitoring - light")
@Composable
private fun LiveMonitoringScreenLightPreview() {
    SafeLabTheme(darkTheme = false) { LiveMonitoringScreen() }
}

@Preview(showBackground = true, name = "Live monitoring - dark")
@Composable
private fun LiveMonitoringScreenDarkPreview() {
    SafeLabTheme(darkTheme = true) { LiveMonitoringScreen() }
}
