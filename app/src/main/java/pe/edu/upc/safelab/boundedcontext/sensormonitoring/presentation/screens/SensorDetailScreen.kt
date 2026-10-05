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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.data.repository.SensorMonitoringRepository
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.CalibrationStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.ReadingStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.Sensor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorCalibration
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorType
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.TelemetryBatch
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.DetailLine
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.ResultMessage
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.SensorHeader
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.SensorSectionCard
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.StatusChip
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.ThresholdBar
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.batchStatusColor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.calibrationStatusColor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.connectionLabel
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.elapsedLabel
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.formatTimestamp
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.readingStatusColor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.sensorStatusColor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.components.targetRangeLabel
import pe.edu.upc.safelab.ui.theme.SafeLabTheme
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

/**
 * User stories covered:
 * - View the detail of a sensor: current reading against its threshold, device and reading status.
 * - Consult the threshold rule, the calibrations (overdue ones highlighted) and the latest telemetry batches.
 * - Register a reading, calibrate the sensor and open its historical readings.
 */
@Composable
fun SensorDetailScreen(
    sensorId: String?,
    onBack: () -> Unit = {},
    onOpenHistory: (String) -> Unit = {}
) {
    val repository = SensorMonitoringRepository
    val sensor = repository.findSensor(sensorId)
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) }

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
                title = sensor?.name ?: "Sensor detail",
                description = if (sensor != null) {
                    "${sensor.type.label} sensor in ${sensor.assetName}, ${sensor.facilityName}."
                } else {
                    "Select a sensor from live monitoring to see its detail."
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

        val calibrations = repository.calibrationsOf(sensor.id)
        val hasOpenCalibration = calibrations.any { it.status != CalibrationStatus.COMPLETED }

        item { CurrentValueCard(sensor) }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ResultMessage(message = message, isError = isError)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            repository.registerReading(sensor.id, sampleValue(sensor))
                                .onSuccess {
                                    message = "Reading ${it.displayValue} registered (${it.status.label})."
                                    isError = false
                                }
                                .onFailure {
                                    message = it.message
                                    isError = true
                                }
                        },
                        enabled = sensor.status == SensorStatus.ACTIVE,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Reading", fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            repository.calibrate(sensor.id, sensor.responsible)
                                .onSuccess {
                                    message = "Calibration completed. Next calibration: ${it.nextCalibrationAt}."
                                    isError = false
                                }
                                .onFailure {
                                    message = it.message
                                    isError = true
                                }
                        },
                        enabled = hasOpenCalibration,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Calibrate", fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                Button(onClick = { onOpenHistory(sensor.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("View history", fontWeight = FontWeight.Bold)
                }
            }
        }

        item { DeviceCard(sensor) }
        item { ThresholdCard(sensor) }
        item { CalibrationsCard(calibrations, repository.nextCalibrationOf(sensor.id)) }
        item { BatchesCard(repository.batchesOf(sensor.id)) }
    }
}

/** Value used by "+ Reading": a normal one for a normal sensor, one over the limit for an out of range sensor. */
private fun sampleValue(sensor: Sensor): Double {
    val threshold = sensor.threshold
    if (sensor.type == SensorType.DOOR_STATUS) return if (sensor.latestReading.value >= 1.0) 0.0 else 1.0

    val span = threshold.maxValue - threshold.minValue
    val raw = if (sensor.readingStatus == ReadingStatus.OUT_OF_RANGE) {
        threshold.maxValue + span * 0.15
    } else {
        val step = SensorMonitoringRepository.readingsOf(sensor.id).size % 3 - 1
        threshold.minValue + span * 0.5 + span * 0.05 * step
    }
    return Math.round(raw * 10) / 10.0
}

@Composable
private fun CurrentValueCard(sensor: Sensor) {
    val readingColor = readingStatusColor(sensor.readingStatus)
    val reading = sensor.latestReading

    SensorSectionCard(title = "Current value", eyebrow = "Reading vs. threshold") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = reading.displayValue,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = readingColor
            )
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusChip(text = sensor.readingStatus.label, color = readingColor)
                StatusChip(text = sensor.status.label, color = sensorStatusColor(sensor.status))
            }
        }
        ThresholdBar(position = sensor.threshold.position(reading.value), color = readingColor)
        DetailLine("Allowed range", targetRangeLabel(sensor))
        DetailLine(
            label = "Last reading",
            value = "${formatTimestamp(reading.recordedAt)} · " +
                elapsedLabel(reading.recordedAt, SensorMonitoringRepository.currentTime())
        )
    }
}

@Composable
private fun DeviceCard(sensor: Sensor) {
    SensorSectionCard(title = "Sensor information") {
        DetailLine("Device status", sensor.status.label, valueColor = sensorStatusColor(sensor.status))
        DetailLine("Connection", connectionLabel(sensor), valueColor = sensorStatusColor(sensor.status))
        DetailLine("Reading status", sensor.readingStatus.label, valueColor = readingStatusColor(sensor.readingStatus))
        DetailLine("Facility", sensor.facilityName)
        DetailLine("Asset", sensor.assetName)
        DetailLine("Responsible", sensor.responsible)
        DetailLine("Installed", sensor.installedAt)
    }
}

@Composable
private fun ThresholdCard(sensor: Sensor) {
    val threshold = sensor.threshold
    SensorSectionCard(title = "Threshold", eyebrow = "Alert limits") {
        DetailLine("Minimum", sensor.type.formatValue(threshold.minValue, threshold.unit))
        DetailLine("Maximum", sensor.type.formatValue(threshold.maxValue, threshold.unit))
        DetailLine("Rule status", threshold.status.label)
    }
}

@Composable
private fun CalibrationsCard(calibrations: List<SensorCalibration>, nextCalibration: String?) {
    val hasOverdue = calibrations.any { it.status == CalibrationStatus.OVERDUE }
    SensorSectionCard(title = "Calibrations", eyebrow = "Maintenance of the device") {
        DetailLine(
            label = "Next calibration",
            value = (nextCalibration ?: "Not scheduled") + if (hasOverdue) " · Overdue" else "",
            valueColor = if (hasOverdue) SafeLabWarning else null
        )
        if (calibrations.isEmpty()) {
            Text(
                text = "This sensor has no calibrations registered.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        calibrations.forEach { CalibrationRow(it) }
    }
}

@Composable
private fun CalibrationRow(calibration: SensorCalibration) {
    val color = calibrationStatusColor(calibration.status)
    val isOverdue = calibration.status == CalibrationStatus.OVERDUE
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isOverdue) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (calibration.calibratedAt != null) {
                        "Calibrated ${formatTimestamp(calibration.calibratedAt)}"
                    } else {
                        "Due ${formatTimestamp(calibration.nextCalibrationAt)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (calibration.calibratedAt != null) {
                        "By ${calibration.performedBy ?: "—"} · next ${formatTimestamp(calibration.nextCalibrationAt)}"
                    } else {
                        "Not performed yet"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(text = calibration.status.label, color = color)
        }
    }
}

@Composable
private fun BatchesCard(batches: List<TelemetryBatch>) {
    SensorSectionCard(title = "Telemetry batches", eyebrow = "Latest received") {
        if (batches.isEmpty()) {
            Text(
                text = "No telemetry batches received yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        batches.forEach { batch ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${batch.id} · ${batch.readingCount} readings",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatTimestamp(batch.receivedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(text = batch.status.label, color = batchStatusColor(batch.status))
            }
        }
    }
}

@Preview(showBackground = true, name = "Sensor detail - light")
@Composable
private fun SensorDetailScreenLightPreview() {
    SafeLabTheme(darkTheme = false) { SensorDetailScreen(sensorId = "sen-003") }
}

@Preview(showBackground = true, name = "Sensor detail - dark")
@Composable
private fun SensorDetailScreenDarkPreview() {
    SafeLabTheme(darkTheme = true) { SensorDetailScreen(sensorId = "sen-003") }
}
