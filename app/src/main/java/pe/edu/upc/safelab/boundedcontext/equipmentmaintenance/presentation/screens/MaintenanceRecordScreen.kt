package pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.data.local.EquipmentMaintenanceMockData
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.data.repository.EquipmentMaintenanceRepository
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.MaintenanceStatus
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.MaintenanceType
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.DetailLine
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.EquipmentHeader
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.EquipmentSectionCard
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.FieldLabel
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.OptionChips
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.ResultMessage
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.SubmitButton
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.components.conditionColor

/**
 * US40 - Register maintenance information of an equipment
 */
@Composable
fun MaintenanceRecordScreen(
    equipmentId: String? = null,
    onBack: () -> Unit = {},
    onSaved: (String) -> Unit = {}
) {
    val repository = EquipmentMaintenanceRepository
    var selectedEquipmentId by rememberSaveable(equipmentId) { mutableStateOf(equipmentId) }
    var typeName by rememberSaveable { mutableStateOf<String?>(null) }
    var statusName by rememberSaveable { mutableStateOf(MaintenanceStatus.COMPLETED.name) }
    var performedAt by rememberSaveable { mutableStateOf("2026-06-19") }
    var performedBy by rememberSaveable { mutableStateOf<String?>(null) }
    var notes by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    val condition = repository.conditionOf(selectedEquipmentId)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            EquipmentHeader(
                eyebrow = "Maintenance record",
                title = "Log maintenance",
                description = "Register the work performed on an equipment so it stays in its maintenance history and in the audit evidence.",
                onBack = onBack,
                backLabel = "Cancel"
            )
        }

        item {
            EquipmentSectionCard(eyebrow = "Step 1", title = "Equipment") {
                OptionChips(
                    options = repository.equipment.map { it.equipmentId to it.name },
                    selected = selectedEquipmentId,
                    onSelected = { selectedEquipmentId = it }
                )
                if (condition != null) {
                    DetailLine("Identifier", condition.equipment.identifier)
                    DetailLine("Location", condition.equipment.location)
                    DetailLine("Current condition", condition.level.label, conditionColor(condition.level))
                }
            }
        }

        item {
            EquipmentSectionCard(eyebrow = "Step 2", title = "Maintenance details") {
                FieldLabel("Type *")
                OptionChips(
                    options = MaintenanceType.entries.map { it.name to it.label },
                    selected = typeName,
                    onSelected = { typeName = it }
                )
                FieldLabel("Status")
                OptionChips(
                    options = MaintenanceStatus.entries.map { it.name to it.label },
                    selected = statusName,
                    onSelected = { if (it != null) statusName = it }
                )
                OutlinedTextField(
                    value = performedAt,
                    onValueChange = { performedAt = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date (YYYY-MM-DD) *") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                FieldLabel("Performed by *")
                OptionChips(
                    options = EquipmentMaintenanceMockData.technicians.map { it to it },
                    selected = performedBy,
                    onSelected = { performedBy = it }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp),
                    label = { Text("Work performed *") },
                    placeholder = { Text("e.g. Door gasket replaced and alarm tested.") },
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        item {
            SubmitButton(
                text = "Save maintenance record",
                onClick = {
                    repository.registerMaintenance(
                        equipmentId = selectedEquipmentId,
                        type = MaintenanceType.entries.firstOrNull { it.name == typeName },
                        status = MaintenanceStatus.valueOf(statusName),
                        performedAt = performedAt,
                        performedBy = performedBy,
                        notes = notes
                    )
                        .onSuccess { record -> onSaved(record.equipmentId) }
                        .onFailure { error -> message = error.message }
                }
            )
        }

        item {
            ResultMessage(message = message, isError = true)
        }
    }
}
