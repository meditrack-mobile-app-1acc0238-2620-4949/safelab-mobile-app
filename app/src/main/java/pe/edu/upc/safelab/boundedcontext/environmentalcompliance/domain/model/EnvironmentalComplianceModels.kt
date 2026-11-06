package pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model

import kotlin.math.roundToInt

/** Sensor reading types that are checked against the allowed conservation range. */
enum class EnvironmentalMetric { TEMPERATURE, HUMIDITY }
enum class EvaluationStatus { COMPLIANT, VIOLATION }
enum class ViolationSeverity { WARNING, CRITICAL }

/** A rule belongs to a monitored asset/area; its bounds are inclusive. */
data class EnvironmentalRule(
    val id: String,
    val nameEn: String,
    val nameEs: String,
    val area: String,
    val equipment: String,
    val sensorId: String,
    val metric: EnvironmentalMetric,
    val minimum: Double,
    val maximum: Double,
    val unit: String,
    val evidenceRequired: Boolean,
    val descriptionEn: String,
    val descriptionEs: String
) {
    init { require(minimum <= maximum) { "Minimum cannot exceed maximum" } }
    fun isWithinRange(value: Double): Boolean = value in minimum..maximum
}

/** An immutable evaluation at one instant, not a live measurement. */
data class EnvironmentalEvaluation(
    val id: String,
    val ruleId: String,
    val observedAt: String, // ISO-8601 local date-time used only for this offline mock.
    val measuredValue: Double,
    val evidenceEn: String,
    val evidenceEs: String
)

data class EnvironmentalViolation(
    val id: String,
    val evaluationId: String,
    val startedAt: String,
    val resolvedAt: String?,
    val durationMinutes: Int?,
    val severity: ViolationSeverity,
    val incidentReference: String?,
    val explanationEn: String,
    val explanationEs: String
) {
    val isActive: Boolean get() = resolvedAt == null
}

data class ComplianceSummary(
    val assessedRules: Int,
    val compliantRules: Int,
    val violatingRules: Int,
    val activeViolations: Int
) {
    val compliancePercent: Int
        get() = if (assessedRules == 0) 0 else (compliantRules * 100.0 / assessedRules).roundToInt()
}
