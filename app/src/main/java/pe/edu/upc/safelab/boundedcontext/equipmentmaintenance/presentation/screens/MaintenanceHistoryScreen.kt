package pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.data.repository.EquipmentMaintenanceRepository
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.MaintenanceRecord
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.MaintenanceStatus
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.MaintenanceType
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.DetailLine
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.EquipmentHeader
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.LevelChip
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.MetricTile
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.OptionChips
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.maintenanceStatusColor
import pe.edu.upc.safelab.ui.theme.SafeLabBlue
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

/**
 * US41 - View the maintenance history of an equipment
 */
@Composable
fun MaintenanceHistoryScreen(
    equipmentId: String? = null,
    onBack: () -> Unit = {},
    onLogMaintenance: (String?) -> Unit = {}
) {
    val repository = EquipmentMaintenanceRepository
    var filterEquipmentId by rememberSaveable(equipmentId) { mutableStateOf(equipmentId) }
    var typeFilter by rememberSaveable { mutableStateOf<String?>(null) }

    val equipment = repository.findEquipment(filterEquipmentId)
    val type = MaintenanceType.entries.firstOrNull { it.name == typeFilter }
    val allForEquipment = repository.history(filterEquipmentId)
    val records = repository.history(filterEquipmentId, type)

    val equipmentOptions = listOf<Pair<String?, String>>(null to "All equipment") +
        repository.equipment.map { it.equipmentId to it.name }
    val typeOptions = listOf<Pair<String?, String>>(null to "All types") +
        MaintenanceType.entries.map { it.name to it.label }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            EquipmentHeader(
                eyebrow = equipment?.identifier ?: "All equipment",
                title = if (equipment != null) equipment.name else "Maintenance history",
                description = if (equipment != null) {
                    "Maintenance history of this equipment, newest first. ${equipment.location}."
                } else {
                    "Preventive, corrective, calibration and inspection work registered for the monitored equipment, newest first."
                },
                onBack = onBack,
                backLabel = "Back to condition"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    label = "Records",
                    value = allForEquipment.size.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabPrimary
                )
                MetricTile(
                    label = "Completed",
                    value = allForEquipment.count { it.status == MaintenanceStatus.COMPLETED }.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabSuccess
                )
                MetricTile(
                    label = "Pending",
                    value = allForEquipment.count { it.status != MaintenanceStatus.COMPLETED }.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabWarning
                )
            }
        }

        item {
            OutlinedButton(onClick = { onLogMaintenance(filterEquipmentId) }) {
                Text("+ Log maintenance", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OptionChips(
                    options = equipmentOptions,
                    selected = filterEquipmentId,
                    onSelected = { filterEquipmentId = it }
                )
                OptionChips(
                    options = typeOptions,
                    selected = typeFilter,
                    onSelected = { typeFilter = it }
                )
            }
        }

        if (records.isEmpty()) {
            item {
                NoDataState(
                    title = "No maintenance records",
                    message = "This equipment has no maintenance registered with the selected filters."
                )
            }
        } else {
            items(records, key = { it.id }) { record ->
                MaintenanceTimelineCard(
                    record = record,
                    equipmentName = repository.findEquipment(record.equipmentId)?.name ?: record.equipmentId,
                    showEquipment = filterEquipmentId == null
                )
            }
        }
    }
}

@Composable
private fun MaintenanceTimelineCard(
    record: MaintenanceRecord,
    equipmentName: String,
    showEquipment: Boolean
) {
    val repository = EquipmentMaintenanceRepository
    val statusColor = maintenanceStatusColor(record.status)
    var addingObservation by rememberSaveable(record.id) { mutableStateOf(false) }
    var observation by rememberSaveable(record.id) { mutableStateOf("") }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 18.dp)
                .size(14.dp)
                .background(statusColor, CircleShape)
        )
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${record.type.label} maintenance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${record.id} · ${record.performedAt}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LevelChip(text = record.status.label, color = statusColor)
                }
                if (showEquipment) {
                    DetailLine("Equipment", equipmentName)
                }
                DetailLine("Performed by", record.performedBy)
                Text(
                    text = record.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                record.observations.forEach { item ->
                    Text(
                        text = "• $item",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafeLabBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (addingObservation) {
                    OutlinedTextField(
                        value = observation,
                        onValueChange = { observation = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Observation") },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (record.status != MaintenanceStatus.COMPLETED) {
                        TextButton(onClick = { repository.complete(record.id) }) {
                            Text("Mark completed", fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(
                        onClick = {
                            if (addingObservation && repository.addObservation(record.id, observation)) {
                                observation = ""
                                addingObservation = false
                            } else {
                                addingObservation = !addingObservation
                            }
                        }
                    ) {
                        Text(
                            text = if (addingObservation) "Save observation" else "Add observation",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
