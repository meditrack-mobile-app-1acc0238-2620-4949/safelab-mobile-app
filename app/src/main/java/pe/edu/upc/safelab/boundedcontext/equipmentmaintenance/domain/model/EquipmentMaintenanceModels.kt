package pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model

/**
 * Equipment Condition & Maintenance bounded context.
 * Condition snapshots, maintenance records and reliability of each monitored equipment.
 * Equipment is referenced by id; its master data belongs to Monitoring Organization.
 */

data class EquipmentReference(
    val equipmentId: String,
    val name: String,
    val identifier: String,
    val location: String
)

/** Snapshot of the equipment condition built from the latest sensor readings. */
data class EquipmentCondition(
    val equipment: EquipmentReference,
    val evaluatedAt: String,
    val indicators: List<ConditionIndicator>,
    val warnings: List<String>
) {
    /** The equipment is abnormal when an indicator is out of limits or a sensor is offline. */
    fun isAbnormal(): Boolean = indicators.any { !it.withinLimits || !it.online }

    val level: ConditionLevel
        get() {
            val offline = indicators.any { !it.online }
            val severeDeviation = indicators.any {
                !it.withinLimits && it.affectsStorage && it.deviationPercent >= 10
            }
            return when {
                offline || severeDeviation -> ConditionLevel.CRITICAL
                isAbnormal() || warnings.isNotEmpty() -> ConditionLevel.ATTENTION
                else -> ConditionLevel.GOOD
            }
        }
}

data class ConditionIndicator(
    val name: String,
    val value: String,
    val allowedRange: String,
    val withinLimits: Boolean,
    val online: Boolean = true,
    /** How far the value is beyond the exceeded limit, in % of that limit (0 when inside). */
    val deviationPercent: Int = 0,
    /** True for storage conditions (temperature, humidity, door); false for mechanical signals. */
    val affectsStorage: Boolean = true
)

enum class ConditionLevel(val label: String) {
    GOOD("Good"),
    ATTENTION("Needs attention"),
    CRITICAL("Critical")
}

data class MaintenanceRecord(
    val id: String,
    val equipmentId: String,
    val type: MaintenanceType,
    val status: MaintenanceStatus,
    val performedAt: String,
    val performedBy: String,
    val notes: String,
    val observations: List<String> = emptyList()
)

enum class MaintenanceType(val label: String) {
    PREVENTIVE("Preventive"),
    CORRECTIVE("Corrective"),
    CALIBRATION("Calibration"),
    INSPECTION("Inspection")
}

enum class MaintenanceStatus(val label: String) {
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed")
}

/** Domain result: reliability of an equipment in a period. */
data class ReliabilityAssessment(
    val equipmentId: String,
    val period: String,
    val score: Int,
    val failureCount: Int,
    val uptimePercent: Double,
    val mtbfHours: Int,
    val mttrHours: Double,
    /** % of time inside the allowed range, one value per week (performance over time). */
    val weeklyStability: List<StabilityPoint>,
    val usage: EquipmentUsage
) {
    fun classify(): ReliabilityLevel = when {
        score >= 85 -> ReliabilityLevel.RELIABLE
        score >= 65 -> ReliabilityLevel.MODERATE
        else -> ReliabilityLevel.AT_RISK
    }
}

data class StabilityPoint(
    val label: String,
    val percentInRange: Float
)

data class EquipmentUsage(
    val hoursPerDay: Double,
    val doorOpeningsPerDay: Int,
    val loadPercent: Int
)

enum class ReliabilityLevel(val label: String) {
    RELIABLE("Reliable"),
    MODERATE("Moderate"),
    AT_RISK("At risk")
}
