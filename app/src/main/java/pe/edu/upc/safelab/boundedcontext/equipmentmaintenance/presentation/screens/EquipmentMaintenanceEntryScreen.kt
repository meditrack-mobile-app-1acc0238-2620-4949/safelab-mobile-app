package pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

private enum class EquipmentDestination {
    CONDITION,
    HISTORY,
    RECORD,
    RELIABILITY
}

/**
 * Entry point of the Equipment Condition & Maintenance module.
 * Condition overview -> maintenance history / new maintenance record / reliability of an equipment.
 */
@Composable
fun EquipmentMaintenanceEntryScreen() {
    var destination by rememberSaveable { mutableStateOf(EquipmentDestination.CONDITION) }
    var selectedEquipmentId by rememberSaveable { mutableStateOf<String?>(null) }
    var previous by rememberSaveable { mutableStateOf(EquipmentDestination.CONDITION) }

    fun navigateTo(next: EquipmentDestination, equipmentId: String?) {
        previous = if (next == EquipmentDestination.RECORD) destination else EquipmentDestination.CONDITION
        selectedEquipmentId = equipmentId
        destination = next
    }

    BackHandler(enabled = destination != EquipmentDestination.CONDITION) {
        destination = if (destination == EquipmentDestination.RECORD) previous else EquipmentDestination.CONDITION
    }

    when (destination) {
        EquipmentDestination.CONDITION -> EquipmentConditionScreen(
            onOpenHistory = { navigateTo(EquipmentDestination.HISTORY, it) },
            onOpenReliability = { navigateTo(EquipmentDestination.RELIABILITY, it) },
            onLogMaintenance = { navigateTo(EquipmentDestination.RECORD, it) }
        )

        EquipmentDestination.HISTORY -> MaintenanceHistoryScreen(
            equipmentId = selectedEquipmentId,
            onBack = { destination = EquipmentDestination.CONDITION },
            onLogMaintenance = { navigateTo(EquipmentDestination.RECORD, it) }
        )

        EquipmentDestination.RECORD -> MaintenanceRecordScreen(
            equipmentId = selectedEquipmentId,
            onBack = { destination = previous },
            onSaved = { equipmentId ->
                selectedEquipmentId = equipmentId
                destination = EquipmentDestination.HISTORY
            }
        )

        EquipmentDestination.RELIABILITY -> EquipmentReliabilityScreen(
            equipmentId = selectedEquipmentId,
            onBack = { destination = EquipmentDestination.CONDITION },
            onOpenHistory = { navigateTo(EquipmentDestination.HISTORY, it) }
        )
    }
}
