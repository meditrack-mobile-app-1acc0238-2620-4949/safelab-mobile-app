package pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model

data class EquipmentReference(
    val equipmentId: String,
    val name: String,
    val identifier: String,
    val location: String
)

enum class Severity(val label: String) {
    INFO("Info"),
    WARNING("Warning"),
    CRITICAL("Critical")
}

enum class AlertStatus(val label: String) {
    ACTIVE("Active"),
    ACKNOWLEDGED("Acknowledged"),
    ESCALATED("Escalated"),
    RESOLVED("Resolved")
}

enum class IncidentStatus(val label: String) {
    OPEN("Open"),
    INVESTIGATING("Investigating"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

enum class MonitoredVariable(val label: String, val unit: String) {
    TEMPERATURE("Temperature", "°C"),
    HUMIDITY("Humidity", "%"),
    VIBRATION("Vibration", "mm/s"),
    DOOR("Door", "")
}

data class AlertRule(
    val ruleId: String,
    val equipmentId: String,
    val variable: MonitoredVariable,
    val min: Double,
    val max: Double,
    val severity: Severity
) {
    val allowedRange: String
        get() = "${format(min)} - ${format(max)} ${variable.unit}".trim()

    fun evaluate(reading: Double): Severity? {
        val margin = (max - min) * NEAR_LIMIT_FRACTION
        return when {
            reading < min || reading > max -> severity
            reading <= min + margin || reading >= max - margin -> Severity.INFO
            else -> null
        }
    }

    fun isOutOfRange(reading: Double): Boolean = reading < min || reading > max

    fun changeThresholds(newMin: Double, newMax: Double, newSeverity: Severity = severity): AlertRule {
        require(newMin < newMax) { "The minimum limit must be lower than the maximum limit." }
        if (variable == MonitoredVariable.HUMIDITY) {
            require(newMin >= 0 && newMax <= 100) { "Humidity limits must be between 0 and 100 %." }
        }
        return copy(min = newMin, max = newMax, severity = newSeverity)
    }

    companion object {
        private const val NEAR_LIMIT_FRACTION = 0.10

        fun format(value: Double): String =
            if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
    }
}

data class Alert(
    val alertId: String,
    val equipmentId: String,
    val sensorId: String,
    val variable: MonitoredVariable,
    val title: String,
    val message: String,
    val value: String,
    val allowedRange: String,
    val severity: Severity,
    val status: AlertStatus,
    val createdAt: String,
    val assignedTo: String,
    val acknowledgedBy: String? = null,
    val acknowledgedAt: String? = null,
    val sharedWith: List<String> = emptyList()
) {
    fun isActive(): Boolean = status != AlertStatus.RESOLVED

    fun isPendingAcknowledgement(): Boolean = status == AlertStatus.ACTIVE

    fun acknowledge(userId: String, at: String): Alert =
        if (!isPendingAcknowledgement()) this
        else copy(status = AlertStatus.ACKNOWLEDGED, acknowledgedBy = userId, acknowledgedAt = at)

    fun escalate(): Alert {
        require(isActive()) { "A resolved alert cannot be escalated." }
        return copy(status = AlertStatus.ESCALATED)
    }

    fun resolve(): Alert = copy(status = AlertStatus.RESOLVED)

    fun shareWith(members: List<String>): Alert {
        require(members.isNotEmpty()) { "Select at least one team member." }
        return copy(sharedWith = (sharedWith + members).distinct())
    }
}

data class CorrectiveAction(
    val actionId: String,
    val description: String,
    val performedBy: String,
    val performedAt: String
)

data class IncidentEvent(
    val at: String,
    val description: String,
    val actor: String
)

data class Incident(
    val incidentId: String,
    val code: String,
    val title: String,
    val description: String,
    val alertId: String,
    val equipmentId: String,
    val severity: Severity,
    val status: IncidentStatus,
    val assignedTo: String,
    val openedAt: String,
    val dueDate: String,
    val closedAt: String? = null,
    val evidenceCount: Int = 0,
    val correctiveActions: List<CorrectiveAction> = emptyList(),
    val timeline: List<IncidentEvent> = emptyList()
) {
    fun isOpen(): Boolean = status == IncidentStatus.OPEN || status == IncidentStatus.INVESTIGATING

    fun startInvestigation(by: String, at: String): Incident {
        require(status == IncidentStatus.OPEN) { "Only an open incident can be investigated." }
        return copy(
            status = IncidentStatus.INVESTIGATING,
            timeline = timeline + IncidentEvent(at, "Status changed to ${IncidentStatus.INVESTIGATING.label}", by)
        )
    }

    fun addCorrectiveAction(action: CorrectiveAction): Incident {
        require(status != IncidentStatus.CLOSED) { "A closed incident cannot receive corrective actions." }
        require(action.description.isNotBlank()) { "Describe the corrective action." }
        return copy(
            correctiveActions = correctiveActions + action,
            timeline = timeline + IncidentEvent(action.performedAt, "Corrective action registered", action.performedBy)
        )
    }

    fun resolve(by: String, at: String): Incident {
        require(isOpen()) { "Only an open or investigating incident can be resolved." }
        require(correctiveActions.isNotEmpty()) { "Register at least one corrective action before resolving." }
        return copy(
            status = IncidentStatus.RESOLVED,
            timeline = timeline + IncidentEvent(at, "Status changed to ${IncidentStatus.RESOLVED.label}", by)
        )
    }

    fun close(by: String, at: String): Incident {
        require(status == IncidentStatus.RESOLVED) { "Resolve the incident before closing it." }
        return copy(
            status = IncidentStatus.CLOSED,
            closedAt = at,
            timeline = timeline + IncidentEvent(at, "Status changed to ${IncidentStatus.CLOSED.label}", by)
        )
    }
}
