package pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.data.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.data.local.EquipmentMaintenanceMockData
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.ConditionLevel
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.EquipmentCondition
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.EquipmentReference
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.MaintenanceRecord
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.MaintenanceStatus
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.MaintenanceType
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.ReliabilityAssessment

/**
 * In-memory repository for TB1. Maintenance records registered in the app are kept
 * while it is open. It will be replaced by a remote repository (SafeLab Platform API).
 */
object EquipmentMaintenanceRepository {

    private val datePattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    val equipment: List<EquipmentReference> = EquipmentMaintenanceMockData.equipment

    val records: SnapshotStateList<MaintenanceRecord> =
        mutableStateListOf(*EquipmentMaintenanceMockData.maintenanceRecords.toTypedArray())

    // ---------- Queries ----------

    fun findEquipment(equipmentId: String?): EquipmentReference? =
        equipment.firstOrNull { it.equipmentId == equipmentId }

    /** US35 - US37: condition of every equipment, most critical first. */
    fun conditions(level: ConditionLevel? = null): List<EquipmentCondition> =
        EquipmentMaintenanceMockData.conditions
            .filter { level == null || it.level == level }
            .sortedByDescending { it.level.ordinal }

    fun conditionOf(equipmentId: String?): EquipmentCondition? =
        EquipmentMaintenanceMockData.conditions.firstOrNull { it.equipment.equipmentId == equipmentId }

    /** US41: maintenance history, newest first, optionally for one equipment. */
    fun history(equipmentId: String? = null, type: MaintenanceType? = null): List<MaintenanceRecord> =
        records
            .filter { equipmentId == null || it.equipmentId == equipmentId }
            .filter { type == null || it.type == type }
            .sortedWith(compareByDescending<MaintenanceRecord> { it.performedAt }.thenByDescending { it.id })

    /** US38, US39, US42: reliability, performance over time and usage. */
    fun reliabilityOf(equipmentId: String?): ReliabilityAssessment? =
        EquipmentMaintenanceMockData.reliability.firstOrNull { it.equipmentId == equipmentId }

    fun lastMaintenanceOf(equipmentId: String): MaintenanceRecord? =
        history(equipmentId).firstOrNull { it.status == MaintenanceStatus.COMPLETED }

    // ---------- Commands ----------

    /** US40: register maintenance information of an equipment. */
    fun registerMaintenance(
        equipmentId: String?,
        type: MaintenanceType?,
        status: MaintenanceStatus,
        performedAt: String,
        performedBy: String?,
        notes: String
    ): Result<MaintenanceRecord> {
        if (findEquipment(equipmentId) == null) return Result.failure(IllegalArgumentException("Select the equipment."))
        if (type == null) return Result.failure(IllegalArgumentException("Select the maintenance type."))
        if (!datePattern.matches(performedAt.trim())) {
            return Result.failure(IllegalArgumentException("Enter the date as YYYY-MM-DD."))
        }
        if (performedBy.isNullOrBlank()) return Result.failure(IllegalArgumentException("Select who performed the maintenance."))
        if (notes.isBlank()) return Result.failure(IllegalArgumentException("Describe the work performed."))

        val record = MaintenanceRecord(
            id = "MNT-%03d".format(records.size + 1),
            equipmentId = equipmentId!!,
            type = type,
            status = status,
            performedAt = performedAt.trim(),
            performedBy = performedBy,
            notes = notes.trim()
        )
        records.add(record)
        return Result.success(record)
    }

    /** MaintenanceRecord.complete() */
    fun complete(recordId: String) {
        val index = records.indexOfFirst { it.id == recordId }
        if (index >= 0) records[index] = records[index].copy(status = MaintenanceStatus.COMPLETED)
    }

    /** MaintenanceRecord.addObservation() */
    fun addObservation(recordId: String, observation: String): Boolean {
        if (observation.isBlank()) return false
        val index = records.indexOfFirst { it.id == recordId }
        if (index < 0) return false
        val record = records[index]
        records[index] = record.copy(observations = record.observations + observation.trim())
        return true
    }
}
