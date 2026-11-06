package pe.edu.upc.safelab.boundedcontext.environmentalcompliance.data.repository

import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.ComplianceSummary
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.EnvironmentalEvaluation
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.EnvironmentalMetric
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.EnvironmentalRule
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.EnvironmentalViolation
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.EvaluationStatus
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.ViolationSeverity

/**
 * A read-only offline data source for the AV2 demonstration.
 * The backend will later provide rules, evaluations and violations through an API.
 * Data and timestamps here are intentionally fictional and never represent live sensor data.
 */
object EnvironmentalComplianceRepository {
    val rules: List<EnvironmentalRule> = listOf(
        EnvironmentalRule("R-TEMP-01", "PCR reagent rack temperature", "Temperatura de reactivos PCR", "Central Clinical Laboratory", "PCR Reagent Rack", "sen-003", EnvironmentalMetric.TEMPERATURE, 2.0, 8.0, "°C", true, "Keep refrigerated samples between 2 and 8 °C.", "Conservar las muestras refrigeradas entre 2 y 8 °C."),
        EnvironmentalRule("R-HUM-02", "Hematology room humidity", "Humedad de sala de hematología", "Central Clinical Laboratory", "Hematology Analyzer Reagents", "sen-002", EnvironmentalMetric.HUMIDITY, 35.0, 60.0, "%", true, "Maintain controlled humidity in the storage area.", "Mantener la humedad controlada en el área de almacenamiento."),
        EnvironmentalRule("R-TEMP-03", "Reagent freezer temperature", "Temperatura de congelador de reactivos", "Central Clinical Laboratory", "Reagent Freezer A", "sen-001", EnvironmentalMetric.TEMPERATURE, 2.0, 8.0, "°C", true, "Keep reagents inside the configured cold range.", "Mantener los reactivos en el rango frío configurado."),
        EnvironmentalRule("R-TEMP-04", "Vaccine fridge temperature", "Temperatura de refrigerador de vacunas", "San Gabriel Hospital Pharmacy", "Vaccine Fridge", "sen-007", EnvironmentalMetric.TEMPERATURE, 2.0, 8.0, "°C", true, "Keep vaccines within the configured cold range.", "Conservar las vacunas dentro del rango frío configurado."),
        EnvironmentalRule("R-HUM-05", "Insulin storage humidity", "Humedad de almacenamiento de insulina", "San Gabriel Hospital Pharmacy", "Insulin Storage", "sen-008", EnvironmentalMetric.HUMIDITY, 35.0, 60.0, "%", true, "Monitor humidity in the insulin storage area.", "Monitorear la humedad del área de almacenamiento de insulina."),
        EnvironmentalRule("R-TEMP-06", "North lab refrigerator temperature", "Temperatura de refrigerador del laboratorio norte", "North Province Clinical Lab", "North Lab Refrigerator", "sen-005", EnvironmentalMetric.TEMPERATURE, 2.0, 8.0, "°C", true, "Monitor the north laboratory refrigerator temperature.", "Monitorear la temperatura del refrigerador del laboratorio norte.")
    )

    val evaluations: List<EnvironmentalEvaluation> = listOf(
        EnvironmentalEvaluation("E-1006", "R-TEMP-01", "2026-06-19T03:10:00", 9.1, "Reading above the configured upper bound.", "Lectura por encima del límite superior configurado."),
        EnvironmentalEvaluation("E-1005", "R-HUM-05", "2026-06-19T04:00:00", 64.0, "Reading above the configured upper bound.", "Lectura por encima del límite superior configurado."),
        EnvironmentalEvaluation("E-1004", "R-HUM-02", "2026-06-19T02:24:00", 46.0, "Reading within the allowed range.", "Lectura dentro del rango permitido."),
        EnvironmentalEvaluation("E-1003", "R-TEMP-03", "2026-06-19T02:25:00", 3.8, "Reading within the allowed range.", "Lectura dentro del rango permitido."),
        EnvironmentalEvaluation("E-1002", "R-TEMP-04", "2026-06-19T02:05:00", 5.1, "Reading within the allowed range.", "Lectura dentro del rango permitido."),
        EnvironmentalEvaluation("E-1001", "R-TEMP-06", "2026-06-19T02:10:00", 4.1, "Reading within the allowed range.", "Lectura dentro del rango permitido."),
        EnvironmentalEvaluation("E-0999", "R-TEMP-04", "2026-06-18T18:10:00", 8.6, "Historic deviation; no longer active.", "Desviación histórica; ya no está activa."),
        EnvironmentalEvaluation("E-0998", "R-TEMP-01", "2026-06-18T18:05:00", 4.5, "Previous compliant reading.", "Lectura anterior conforme.")
    )

    val violations: List<EnvironmentalViolation> = listOf(
        EnvironmentalViolation("V-210", "E-1006", "2026-11-05T21:40:00", null, null, ViolationSeverity.CRITICAL, "INC-001", "PCR reagent rack exceeded the temperature limit; review the linked incident.", "Los reactivos PCR superaron el límite de temperatura; revisar el incidente vinculado."),
        EnvironmentalViolation("V-209", "E-1005", "2026-11-05T21:35:00", null, null, ViolationSeverity.WARNING, "INC-002", "Insulin storage humidity exceeded the configured maximum.", "La humedad de almacenamiento de insulina superó el máximo configurado."),
        EnvironmentalViolation("V-208", "E-0999", "2026-06-18T18:10:00", "2026-06-18T18:35:00", 25, ViolationSeverity.WARNING, null, "Temperature returned to the permitted range after 25 minutes.", "La temperatura volvió al rango permitido después de 25 minutos.")
    )

    fun rule(id: String): EnvironmentalRule? = rules.find { it.id == id }
    fun evaluation(id: String): EnvironmentalEvaluation? = evaluations.find { it.id == id }
    fun violation(id: String): EnvironmentalViolation? = violations.find { it.id == id }

    fun status(evaluation: EnvironmentalEvaluation): EvaluationStatus {
        val rule = rule(evaluation.ruleId) ?: return EvaluationStatus.VIOLATION
        return if (rule.isWithinRange(evaluation.measuredValue)) EvaluationStatus.COMPLIANT
        else EvaluationStatus.VIOLATION
    }

    /** Every rule is assessed using its newest sample; older evaluations remain in history. */
    fun latestEvaluation(ruleId: String): EnvironmentalEvaluation? = evaluations
        .filter { it.ruleId == ruleId }
        .maxByOrNull { it.observedAt }

    fun summary(): ComplianceSummary {
        val latest = rules.mapNotNull { latestEvaluation(it.id) }
        val compliant = latest.count { status(it) == EvaluationStatus.COMPLIANT }
        return ComplianceSummary(latest.size, compliant, latest.size - compliant,
            violations.count { it.isActive })
    }

    fun findRules(query: String, onlyViolations: Boolean = false): List<EnvironmentalRule> = rules.filter { rule ->
        val matches = listOf(rule.id, rule.nameEn, rule.nameEs, rule.area, rule.equipment, rule.sensorId)
            .any { it.contains(query.trim(), ignoreCase = true) }
        val isViolation = latestEvaluation(rule.id)?.let { status(it) == EvaluationStatus.VIOLATION } ?: false
        matches && (!onlyViolations || isViolation)
    }
}
