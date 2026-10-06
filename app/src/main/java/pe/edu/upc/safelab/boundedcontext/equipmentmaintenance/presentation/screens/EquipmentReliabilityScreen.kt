package pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.data.repository.EquipmentMaintenanceRepository
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.ReliabilityLevel
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.DetailLine
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.EquipmentHeader
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.EquipmentSectionCard
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.MetricTile
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.OptionChips
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.ProgressLine
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.ScoreRing
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.StabilityChart
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.reliabilityColor
import pe.edu.upc.safelab.ui.theme.SafeLabAccent
import pe.edu.upc.safelab.ui.theme.SafeLabDanger
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess

/**
 * US38 - View equipment performance over time
 * US39 - View equipment usage data
 * US42 - View equipment stability over time (reliability)
 */
@Composable
fun EquipmentReliabilityScreen(
    equipmentId: String? = null,
    onBack: () -> Unit = {},
    onOpenHistory: (String) -> Unit = {}
) {
    val repository = EquipmentMaintenanceRepository
    var selectedId by rememberSaveable(equipmentId) {
        mutableStateOf(equipmentId ?: repository.equipment.first().equipmentId)
    }

    val equipment = repository.findEquipment(selectedId)
    val reliability = repository.reliabilityOf(selectedId)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            EquipmentHeader(
                eyebrow = equipment?.identifier ?: "Reliability",
                title = equipment?.name ?: "Equipment reliability",
                description = "Stability, failures and usage of the equipment in the ${reliability?.period?.lowercase() ?: "selected period"}.",
                onBack = onBack,
                backLabel = "Back to condition"
            )
        }

        item {
            OptionChips(
                options = repository.equipment.map { it.equipmentId to it.name },
                selected = selectedId,
                onSelected = { if (it != null) selectedId = it }
            )
        }

        if (reliability == null) {
            item {
                NoDataState(
                    title = "No reliability data yet",
                    message = "This equipment needs at least one week of readings to calculate its reliability."
                )
            }
        } else {
            val level = reliability.classify()
            val color = reliabilityColor(level)

            item {
                EquipmentSectionCard(eyebrow = reliability.period, title = "Reliability score") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreRing(score = reliability.score, color = color, caption = level.label)
                        Text(
                            text = when (level) {
                                ReliabilityLevel.RELIABLE -> "Stable operation. Keep the preventive maintenance plan."
                                ReliabilityLevel.MODERATE -> "Some deviations in the period. Review the recent warnings."
                                ReliabilityLevel.AT_RISK -> "Frequent failures. Schedule corrective maintenance."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricTile(
                        label = "Failures",
                        value = reliability.failureCount.toString(),
                        modifier = Modifier.weight(1f),
                        accent = if (reliability.failureCount == 0) SafeLabSuccess else SafeLabDanger
                    )
                    MetricTile(
                        label = "MTBF",
                        value = "${reliability.mtbfHours} h",
                        modifier = Modifier.weight(1f),
                        accent = SafeLabPrimary,
                        supportingText = "Between failures"
                    )
                    MetricTile(
                        label = "MTTR",
                        value = "${reliability.mttrHours} h",
                        modifier = Modifier.weight(1f),
                        accent = SafeLabAccent,
                        supportingText = "To repair"
                    )
                }
            }

            item {
                EquipmentSectionCard(eyebrow = "Performance over time", title = "Time within allowed range") {
                    StabilityChart(points = reliability.weeklyStability)
                    Text(
                        text = "Weekly percentage of readings inside the allowed range.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                EquipmentSectionCard(eyebrow = "Usage", title = "Usage data") {
                    ProgressLine(
                        label = "Uptime",
                        valueText = "${reliability.uptimePercent} %",
                        fraction = (reliability.uptimePercent / 100.0).toFloat(),
                        color = SafeLabSuccess
                    )
                    ProgressLine(
                        label = "Operating hours per day",
                        valueText = "${reliability.usage.hoursPerDay} h",
                        fraction = (reliability.usage.hoursPerDay / 24.0).toFloat(),
                        color = SafeLabPrimary
                    )
                    ProgressLine(
                        label = "Storage load",
                        valueText = "${reliability.usage.loadPercent} %",
                        fraction = reliability.usage.loadPercent / 100f,
                        color = SafeLabAccent
                    )
                    DetailLine("Door openings per day", reliability.usage.doorOpeningsPerDay.toString())
                }
            }

            item {
                OutlinedButton(
                    onClick = { onOpenHistory(selectedId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View maintenance history", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
