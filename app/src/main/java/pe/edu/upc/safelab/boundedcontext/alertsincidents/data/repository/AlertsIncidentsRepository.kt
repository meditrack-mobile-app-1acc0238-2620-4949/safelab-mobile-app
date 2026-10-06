package pe.edu.upc.safelab.boundedcontext.alertsincidents.data.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import pe.edu.upc.safelab.boundedcontext.alertsincidents.data.local.AlertsIncidentsMockData
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.Alert
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.AlertRule
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.AlertStatus
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.CorrectiveAction
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.EquipmentReference
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.Incident
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.IncidentEvent
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.IncidentStatus
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.Severity

/**
 * In-memory repository for TB1. Alerts, rules and incidents changed in the app are kept
 * while it is open. It will be replaced by a remote repository (SafeLab Platform API).
 */
object AlertsIncidentsRepository {

    private val timestampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    val currentUser: String = AlertsIncidentsMockData.CURRENT_USER

    val teamMembers: List<String> = AlertsIncidentsMockData.teamMembers

    val equipment: List<EquipmentReference> = AlertsIncidentsMockData.equipment

    val rules: SnapshotStateList<AlertRule> =
        mutableStateListOf(*AlertsIncidentsMockData.alertRules.toTypedArray())

    val alerts: SnapshotStateList<Alert> =
        mutableStateListOf(*AlertsIncidentsMockData.alerts.toTypedArray())

    val incidents: SnapshotStateList<Incident> =
        mutableStateListOf(*AlertsIncidentsMockData.incidents.toTypedArray())

    private fun now(): String = LocalDateTime.now().format(timestampFormat)

    // ---------- Queries ----------

    fun findEquipment(equipmentId: String?): EquipmentReference? =
        equipment.firstOrNull { it.equipmentId == equipmentId }

    fun alerts(
        severity: Severity? = null,
        status: AlertStatus? = null,
        query: String = "",
        sortBySeverity: Boolean = true
    ): List<Alert> {
        val text = query.trim().lowercase()
        val filtered = alerts
            .filter { severity == null || it.severity == severity }
            .filter { status == null || it.status == status }
            .filter {
                text.isEmpty() ||
                    it.title.lowercase().contains(text) ||
                    it.message.lowercase().contains(text) ||
                    (findEquipment(it.equipmentId)?.name?.lowercase()?.contains(text) == true)
            }
        val newestFirst = compareByDescending<Alert> { it.createdAt }.thenByDescending { it.alertId }
        return if (sortBySeverity) {
            filtered.sortedWith(compareByDescending<Alert> { it.severity.ordinal }.then(newestFirst))
        } else {
            filtered.sortedWith(newestFirst)
        }
    }

    fun findAlert(alertId: String?): Alert? = alerts.firstOrNull { it.alertId == alertId }

    fun criticalActiveAlerts(): List<Alert> =
        alerts.filter { it.severity == Severity.CRITICAL && it.isActive() }

    fun pendingAcknowledgementCount(): Int = alerts.count { it.isPendingAcknowledgement() }

    fun equipmentWithActiveAlerts(): List<Pair<EquipmentReference, List<Alert>>> =
        alerts
            .filter { it.isActive() }
            .groupBy { it.equipmentId }
            .mapNotNull { (equipmentId, equipmentAlerts) ->
                findEquipment(equipmentId)?.let { it to equipmentAlerts }
            }
            .sortedByDescending { (_, equipmentAlerts) -> equipmentAlerts.maxOf { it.severity.ordinal } }

    fun rulesOf(equipmentId: String?): List<AlertRule> = rules.filter { it.equipmentId == equipmentId }

    fun incidentHistory(status: IncidentStatus? = null, severity: Severity? = null): List<Incident> =
        incidents
            .filter { status == null || it.status == status }
            .filter { severity == null || it.severity == severity }
            .sortedWith(compareByDescending<Incident> { it.openedAt }.thenByDescending { it.code })

    fun findIncident(incidentId: String?): Incident? = incidents.firstOrNull { it.incidentId == incidentId }

    fun incidentOfAlert(alertId: String?): Incident? = incidents.firstOrNull { it.alertId == alertId }

    // ---------- Commands: alerts ----------

    fun acknowledgeAlert(alertId: String): Result<Alert> {
        val index = alerts.indexOfFirst { it.alertId == alertId }
        if (index < 0) return Result.failure(IllegalArgumentException("The alert cannot be found."))
        val alert = alerts[index]
        if (!alert.isPendingAcknowledgement()) {
            return Result.failure(IllegalArgumentException("The alert was already attended (${alert.status.label})."))
        }
        val acknowledged = alert.acknowledge(currentUser, now())
        alerts[index] = acknowledged
        return Result.success(acknowledged)
    }

    fun resolveAlert(alertId: String): Result<Alert> {
        val index = alerts.indexOfFirst { it.alertId == alertId }
        if (index < 0) return Result.failure(IllegalArgumentException("The alert cannot be found."))
        if (!alerts[index].isActive()) return Result.failure(IllegalArgumentException("The alert is already resolved."))
        val resolved = alerts[index].resolve()
        alerts[index] = resolved
        return Result.success(resolved)
    }

    fun shareAlert(alertId: String, members: List<String>): Result<Alert> {
        val index = alerts.indexOfFirst { it.alertId == alertId }
        if (index < 0) return Result.failure(IllegalArgumentException("The alert is not available."))
        return runCatching { alerts[index].shareWith(members) }
            .onSuccess { alerts[index] = it }
    }

    fun escalateAlert(alertId: String): Result<Incident> {
        val index = alerts.indexOfFirst { it.alertId == alertId }
        if (index < 0) return Result.failure(IllegalArgumentException("The alert cannot be found."))
        return runCatching {
            val escalated = alerts[index].escalate()
            alerts[index] = escalated
            incidentOfAlert(alertId) ?: openIncident(escalated)
        }
    }

    private fun openIncident(alert: Alert): Incident {
        val openedAt = now()
        val number = incidents.size + 1
        val incident = Incident(
            incidentId = "inc-%03d".format(number),
            code = "INC-%03d".format(number),
            title = alert.title,
            description = alert.message,
            alertId = alert.alertId,
            equipmentId = alert.equipmentId,
            severity = alert.severity,
            status = IncidentStatus.OPEN,
            assignedTo = alert.assignedTo,
            openedAt = openedAt,
            dueDate = LocalDate.now().plusDays(1).toString(),
            timeline = listOf(IncidentEvent(openedAt, "Incident opened from alert ${alert.alertId}", currentUser))
        )
        incidents.add(incident)
        return incident
    }

    // ---------- Commands: rules ----------

    fun changeThresholds(ruleId: String, min: String, max: String, severity: Severity?): Result<AlertRule> {
        val index = rules.indexOfFirst { it.ruleId == ruleId }
        if (index < 0) return Result.failure(IllegalArgumentException("Select a rule of a registered equipment."))
        val minValue = min.trim().replace(',', '.').toDoubleOrNull()
            ?: return Result.failure(IllegalArgumentException("Enter a valid minimum limit."))
        val maxValue = max.trim().replace(',', '.').toDoubleOrNull()
            ?: return Result.failure(IllegalArgumentException("Enter a valid maximum limit."))
        if (severity == null) return Result.failure(IllegalArgumentException("Select the alert severity."))
        return runCatching { rules[index].changeThresholds(minValue, maxValue, severity) }
            .onSuccess { rules[index] = it }
    }

    fun evaluateReading(ruleId: String, reading: String): Result<Alert?> {
        val rule = rules.firstOrNull { it.ruleId == ruleId }
            ?: return Result.failure(IllegalArgumentException("Select a rule of a registered equipment."))
        val value = reading.trim().replace(',', '.').toDoubleOrNull()
            ?: return Result.failure(IllegalArgumentException("Enter a valid reading."))
        val severity = rule.evaluate(value) ?: return Result.success(null)
        val equipmentName = findEquipment(rule.equipmentId)?.name ?: rule.equipmentId
        val sensorId = AlertsIncidentsMockData.sensors
            .firstOrNull { it.equipmentId == rule.equipmentId && it.variable == rule.variable }?.sensorId ?: "-"
        val outOfRange = rule.isOutOfRange(value)
        val alert = Alert(
            alertId = "alert-%03d".format(alerts.size + 1),
            equipmentId = rule.equipmentId,
            sensorId = sensorId,
            variable = rule.variable,
            title = if (outOfRange) "$equipmentName out of range" else "$equipmentName near the limit",
            message = "${rule.variable.label} reading of ${AlertRule.format(value)} ${rule.variable.unit}".trim() +
                if (outOfRange) " is outside the allowed range." else " is approaching the allowed limit.",
            value = "${AlertRule.format(value)} ${rule.variable.unit}".trim(),
            allowedRange = rule.allowedRange,
            severity = severity,
            status = AlertStatus.ACTIVE,
            createdAt = now(),
            assignedTo = currentUser
        )
        alerts.add(alert)
        return Result.success(alert)
    }

    // ---------- Commands: incidents ----------

    fun startInvestigation(incidentId: String): Result<Incident> =
        updateIncident(incidentId) { it.startInvestigation(currentUser, now()) }

    fun registerCorrectiveAction(incidentId: String, description: String, performedBy: String?): Result<Incident> {
        if (description.isBlank()) return Result.failure(IllegalArgumentException("Describe the corrective action."))
        if (performedBy.isNullOrBlank()) return Result.failure(IllegalArgumentException("Select who performed the action."))
        val actionNumber = incidents.sumOf { it.correctiveActions.size } + 1
        val action = CorrectiveAction(
            actionId = "ACT-%03d".format(actionNumber),
            description = description.trim(),
            performedBy = performedBy,
            performedAt = now()
        )
        return updateIncident(incidentId) { it.addCorrectiveAction(action) }
    }

    fun resolveIncident(incidentId: String): Result<Incident> =
        updateIncident(incidentId) { it.resolve(currentUser, now()) }
            .onSuccess { incident -> resolveAlert(incident.alertId) }

    fun closeIncident(incidentId: String): Result<Incident> =
        updateIncident(incidentId) { it.close(currentUser, now()) }
            .onSuccess { incident -> resolveAlert(incident.alertId) }

    private fun updateIncident(incidentId: String, change: (Incident) -> Incident): Result<Incident> {
        val index = incidents.indexOfFirst { it.incidentId == incidentId }
        if (index < 0) return Result.failure(IllegalArgumentException("The incident cannot be found."))
        return runCatching { change(incidents[index]) }
            .onSuccess { incidents[index] = it }
    }
}
