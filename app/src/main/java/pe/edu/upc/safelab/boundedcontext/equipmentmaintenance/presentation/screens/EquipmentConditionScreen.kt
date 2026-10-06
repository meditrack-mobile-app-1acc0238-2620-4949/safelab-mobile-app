package pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.screens

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.data.repository.EquipmentMaintenanceRepository
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.ConditionLevel
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.EquipmentCondition
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.DetailLine
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.EquipmentHeader
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.LevelChip
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.MetricTile
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.OptionChips
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.conditionColor
import pe.edu.upc.safelab.ui.theme.SafeLabDanger
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

/**
 * US35 - View equipment condition
 * US36 - Identify environmental values out of limits
 * US37 - Receive warnings about the equipment
 */
@Composable
fun EquipmentConditionScreen(
    onOpenHistory: (String?) -> Unit = {},
    onOpenReliability: (String) -> Unit = {},
    onLogMaintenance: (String?) -> Unit = {}
) {
    val repository = EquipmentMaintenanceRepository
    var levelFilter by rememberSaveable { mutableStateOf<String?>(null) }

    val all = repository.conditions()
    val visible = repository.conditions(ConditionLevel.entries.firstOrNull { it.name == levelFilter })
    val filterOptions = listOf<Pair<String?, String>>(null to "All (${all.size})") +
        ConditionLevel.entries.reversed().map { level ->
            level.name to "${level.label} (${all.count { it.level == level }})"
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            EquipmentHeader(
                eyebrow = "Equipment condition & maintenance",
                title = "Equipment condition",
                description = "Condition of each monitored equipment based on its latest readings. Values out of the allowed range and active warnings are shown first."
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    label = "Critical",
                    value = all.count { it.level == ConditionLevel.CRITICAL }.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabDanger
                )
                MetricTile(
                    label = "Attention",
                    value = all.count { it.level == ConditionLevel.ATTENTION }.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabWarning
                )
                MetricTile(
                    label = "Good",
                    value = all.count { it.level == ConditionLevel.GOOD }.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabSuccess
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(onClick = { onOpenHistory(null) }, modifier = Modifier.weight(1f)) {
                    Text("Maintenance history", fontWeight = FontWeight.Bold, maxLines = 1)
                }
                OutlinedButton(onClick = { onLogMaintenance(null) }, modifier = Modifier.weight(1f)) {
                    Text("+ Log maintenance", fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }

        item {
            OptionChips(
                options = filterOptions,
                selected = levelFilter,
                onSelected = { levelFilter = it }
            )
        }

        if (visible.isEmpty()) {
            item {
                NoDataState(
                    title = "No equipment in this condition",
                    message = "Choose another filter to see the rest of the equipment."
                )
            }
        } else {
            items(visible, key = { it.equipment.equipmentId }) { condition ->
                ConditionCard(
                    condition = condition,
                    lastMaintenance = repository.lastMaintenanceOf(condition.equipment.equipmentId)?.performedAt,
                    onOpenHistory = { onOpenHistory(condition.equipment.equipmentId) },
                    onOpenReliability = { onOpenReliability(condition.equipment.equipmentId) },
                    onLogMaintenance = { onLogMaintenance(condition.equipment.equipmentId) }
                )
            }
        }
    }
}

@Composable
private fun ConditionCard(
    condition: EquipmentCondition,
    lastMaintenance: String?,
    onOpenHistory: () -> Unit,
    onOpenReliability: () -> Unit,
    onLogMaintenance: () -> Unit
) {
    val level = condition.level
    val color = conditionColor(level)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = condition.equipment.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${condition.equipment.identifier} · ${condition.equipment.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                LevelChip(text = level.label, color = color)
            }

            condition.indicators.forEach { indicator ->
                val indicatorColor = when {
                    !indicator.online -> SafeLabDanger
                    indicator.withinLimits -> SafeLabSuccess
                    else -> conditionColor(level)
                }
                val status = when {
                    !indicator.online -> "Sensor offline"
                    indicator.withinLimits -> "In range"
                    else -> "Out of range"
                }
                DetailLine(
                    label = "${indicator.name} (${indicator.allowedRange})",
                    value = "${indicator.value} · $status",
                    valueColor = indicatorColor
                )
            }

            condition.warnings.forEach { warning ->
                Text(
                    text = "△ $warning",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            DetailLine("Evaluated", condition.evaluatedAt)
            DetailLine("Last maintenance", lastMaintenance ?: "No records")

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                TextButton(onClick = onOpenReliability) { Text("Reliability", fontWeight = FontWeight.Bold) }
                TextButton(onClick = onOpenHistory) { Text("History", fontWeight = FontWeight.Bold) }
                TextButton(onClick = onLogMaintenance) { Text("+ Log", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
