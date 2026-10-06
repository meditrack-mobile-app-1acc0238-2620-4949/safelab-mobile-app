package pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

private enum class SensorDestination {
    LIVE,
    DETAIL,
    HISTORY,
    OFFLINE
}

/**
 * Entry point of the Sensor Monitoring module.
 * Live monitoring -> sensor detail -> historical readings,
 * and live monitoring -> offline sensors -> sensor detail.
 */
@Composable
fun SensorMonitoringEntryScreen() {
    var destination by rememberSaveable { mutableStateOf(SensorDestination.LIVE) }
    var selectedSensorId by rememberSaveable { mutableStateOf<String?>(null) }
    // Screen from which the detail was opened (live monitoring or offline sensors).
    var detailOrigin by rememberSaveable { mutableStateOf(SensorDestination.LIVE) }

    fun openDetail(sensorId: String) {
        detailOrigin = destination
        selectedSensorId = sensorId
        destination = SensorDestination.DETAIL
    }

    fun goBack() {
        destination = when (destination) {
            SensorDestination.HISTORY -> SensorDestination.DETAIL
            SensorDestination.DETAIL -> detailOrigin
            else -> SensorDestination.LIVE
        }
    }

    BackHandler(enabled = destination != SensorDestination.LIVE) { goBack() }

    when (destination) {
        SensorDestination.LIVE -> LiveMonitoringScreen(
            onOpenDetail = { openDetail(it) },
            onOpenOffline = { destination = SensorDestination.OFFLINE }
        )

        SensorDestination.DETAIL -> SensorDetailScreen(
            sensorId = selectedSensorId,
            onBack = { goBack() },
            onOpenHistory = { sensorId ->
                selectedSensorId = sensorId
                destination = SensorDestination.HISTORY
            }
        )

        SensorDestination.HISTORY -> HistoricalReadingsScreen(
            sensorId = selectedSensorId,
            onBack = { goBack() }
        )

        SensorDestination.OFFLINE -> OfflineSensorsScreen(
            onBack = { goBack() },
            onOpenDetail = { openDetail(it) }
        )
    }
}
