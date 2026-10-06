package pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.data.repository.SensorMonitoringRepository
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.Sensor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.DetailLine
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.SensorHeader
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.StatusChip
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.elapsedLabel
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.formatTimestamp
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.sensorStatusColor
import pe.edu.upc.safelab.ui.theme.SafeLabTheme

/**
 * User stories covered:
 * - Identify the sensors that are not sending data (disconnected, in maintenance or inactive).
 * - See how long ago each one sent its last reading and open its detail.
 */
@Composable
fun OfflineSensorsScreen(
    onBack: () -> Unit = {},
    onOpenDetail: (String) -> Unit = {}
) {
    val sensors = SensorMonitoringRepository.offlineSensors()

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
                title = "Offline sensors",
                description = "Sensors that are not sending readings. Disconnected sensors come first, then those in maintenance or inactive.",
                onBack = onBack,
                backLabel = "Live monitoring"
            )
        }

        if (sensors.isEmpty()) {
            item {
                NoDataState(
                    title = "All sensors are online",
                    message = "There are no disconnected, inactive or in-maintenance sensors right now."
                )
            }
        } else {
            items(sensors, key = { it.id }) { sensor ->
                OfflineSensorCard(sensor = sensor, onClick = { onOpenDetail(sensor.id) })
            }
        }
    }
}

@Composable
private fun OfflineSensorCard(sensor: Sensor, onClick: () -> Unit) {
    val statusColor = sensorStatusColor(sensor.status)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
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
                        text = "${sensor.code} · ${sensor.assetName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusChip(text = sensor.status.label, color = statusColor)
            }

            DetailLine(
                label = "Last reading",
                value = elapsedLabel(sensor.latestReading.recordedAt, SensorMonitoringRepository.currentTime()),
                valueColor = statusColor
            )
            DetailLine("Recorded at", formatTimestamp(sensor.latestReading.recordedAt))
            DetailLine("Last value", sensor.latestReading.displayValue)
            DetailLine("Facility", sensor.facilityName)
            DetailLine("Responsible", sensor.responsible)

            TextButton(onClick = onClick) {
                Text("View detail", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, name = "Offline sensors - light")
@Composable
private fun OfflineSensorsScreenLightPreview() {
    SafeLabTheme(darkTheme = false) { OfflineSensorsScreen() }
}

@Preview(showBackground = true, name = "Offline sensors - dark")
@Composable
private fun OfflineSensorsScreenDarkPreview() {
    SafeLabTheme(darkTheme = true) { OfflineSensorsScreen() }
}
