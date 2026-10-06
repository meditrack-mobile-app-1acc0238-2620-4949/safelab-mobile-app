package pe.edu.upc.safelab.boundedcontext.alertsincidents.data.local

import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.Alert
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.AlertRule
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.AlertStatus
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.CorrectiveAction
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.EquipmentReference
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.Incident
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.IncidentEvent
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.IncidentStatus
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.MonitoredVariable
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.Severity

data class SensorReference(
    val sensorId: String,
    val equipmentId: String,
    val variable: MonitoredVariable
)

object AlertsIncidentsMockData {

    const val CURRENT_USER = "Dr. Maria Lopez"

    val teamMembers = listOf(
        "Carlos Mendoza",
        "Andrea Torres",
        "Patricia Rivas",
        "Rosa Fernandez",
        "Gabriel Paredes",
        "Valeria Rojas"
    )

    val equipment = listOf(
        EquipmentReference("asset-001", "Reagent Freezer A", "EQ-CEN-001", "Storage 1 · Central Clinical Laboratory"),
        EquipmentReference("asset-002", "PCR Reagent Rack", "EQ-CEN-002", "PCR Room · Central Clinical Laboratory"),
        EquipmentReference("asset-003", "Hematology Analyzer Reagents", "EQ-CEN-003", "Hematology · Central Clinical Laboratory"),
        EquipmentReference("asset-004", "Sample Storage Door", "EQ-CEN-004", "Sample Room · Central Clinical Laboratory"),
        EquipmentReference("asset-005", "North Lab Refrigerator", "EQ-NOR-001", "Storage · North Province Clinical Lab"),
        EquipmentReference("asset-006", "North Incubator", "EQ-NOR-002", "Culture Room · North Province Clinical Lab"),
        EquipmentReference("asset-007", "Vaccine Fridge", "EQ-HSP-001", "Vaccines · San Gabriel Hospital Pharmacy"),
        EquipmentReference("asset-008", "Insulin Storage", "EQ-HSP-002", "Insulin · San Gabriel Hospital Pharmacy"),
        EquipmentReference("asset-009", "Controlled Drugs Cabinet", "EQ-HSP-003", "Restricted Area · San Gabriel Hospital Pharmacy"),
        EquipmentReference("asset-010", "PharmAndina Cold Room", "EQ-PHA-001", "Cold Chain · PharmAndina Cold Chain")
    )

    val sensors = listOf(
        SensorReference("sen-001", "asset-001", MonitoredVariable.TEMPERATURE),
        SensorReference("sen-002", "asset-003", MonitoredVariable.HUMIDITY),
        SensorReference("sen-003", "asset-002", MonitoredVariable.TEMPERATURE),
        SensorReference("sen-004", "asset-004", MonitoredVariable.DOOR),
        SensorReference("sen-005", "asset-005", MonitoredVariable.TEMPERATURE),
        SensorReference("sen-006", "asset-006", MonitoredVariable.VIBRATION),
        SensorReference("sen-007", "asset-007", MonitoredVariable.TEMPERATURE),
        SensorReference("sen-008", "asset-008", MonitoredVariable.HUMIDITY),
        SensorReference("sen-009", "asset-009", MonitoredVariable.DOOR),
        SensorReference("sen-010", "asset-010", MonitoredVariable.TEMPERATURE)
    )

    val alertRules = listOf(
        AlertRule("RUL-001", "asset-001", MonitoredVariable.TEMPERATURE, 2.0, 8.0, Severity.CRITICAL),
        AlertRule("RUL-002", "asset-002", MonitoredVariable.TEMPERATURE, 2.0, 8.0, Severity.CRITICAL),
        AlertRule("RUL-003", "asset-003", MonitoredVariable.HUMIDITY, 35.0, 60.0, Severity.WARNING),
        AlertRule("RUL-004", "asset-005", MonitoredVariable.TEMPERATURE, 2.0, 8.0, Severity.CRITICAL),
        AlertRule("RUL-005", "asset-006", MonitoredVariable.VIBRATION, 0.0, 3.0, Severity.WARNING),
        AlertRule("RUL-006", "asset-007", MonitoredVariable.TEMPERATURE, 2.0, 8.0, Severity.CRITICAL),
        AlertRule("RUL-007", "asset-008", MonitoredVariable.HUMIDITY, 35.0, 60.0, Severity.WARNING),
        AlertRule("RUL-008", "asset-010", MonitoredVariable.TEMPERATURE, 2.0, 8.0, Severity.CRITICAL)
    )

    val alerts = listOf(
        Alert(
            alertId = "alert-001",
            equipmentId = "asset-002",
            sensorId = "sen-003",
            variable = MonitoredVariable.TEMPERATURE,
            title = "PCR freezer out of range",
            message = "PCR freezer exceeded maximum temperature.",
            value = "9.1 °C",
            allowedRange = "2 - 8 °C",
            severity = Severity.CRITICAL,
            status = AlertStatus.ACTIVE,
            createdAt = "2026-06-19 03:10",
            assignedTo = "Carlos Mendoza"
        ),
        Alert(
            alertId = "alert-002",
            equipmentId = "asset-008",
            sensorId = "sen-008",
            variable = MonitoredVariable.HUMIDITY,
            title = "Insulin humidity deviation",
            message = "Insulin storage requires validation.",
            value = "64 %",
            allowedRange = "35 - 60 %",
            severity = Severity.WARNING,
            status = AlertStatus.ACTIVE,
            createdAt = "2026-06-19 04:10",
            assignedTo = "Rosa Fernandez",
            sharedWith = listOf("Rosa Fernandez")
        ),
        Alert(
            alertId = "alert-003",
            equipmentId = "asset-009",
            sensorId = "sen-009",
            variable = MonitoredVariable.DOOR,
            title = "Cabinet door open",
            message = "Controlled drug cabinet door remains open.",
            value = "Open",
            allowedRange = "Closed",
            severity = Severity.CRITICAL,
            status = AlertStatus.ACKNOWLEDGED,
            createdAt = "2026-06-19 05:10",
            assignedTo = "Gabriel Paredes",
            acknowledgedBy = "Gabriel Paredes",
            acknowledgedAt = "2026-06-19 05:18",
            sharedWith = listOf("Gabriel Paredes", "Rosa Fernandez")
        ),
        Alert(
            alertId = "alert-004",
            equipmentId = "asset-006",
            sensorId = "sen-006",
            variable = MonitoredVariable.VIBRATION,
            title = "Incubator vibration warning",
            message = "North incubator has abnormal vibration readings.",
            value = "4.8 mm/s",
            allowedRange = "0 - 3 mm/s",
            severity = Severity.WARNING,
            status = AlertStatus.ACTIVE,
            createdAt = "2026-06-19 06:10",
            assignedTo = "Patricia Rivas"
        ),
        Alert(
            alertId = "alert-005",
            equipmentId = "asset-007",
            sensorId = "sen-007",
            variable = MonitoredVariable.TEMPERATURE,
            title = "Vaccine fridge stable",
            message = "Vaccine fridge temperature is stable.",
            value = "5.1 °C",
            allowedRange = "2 - 8 °C",
            severity = Severity.INFO,
            status = AlertStatus.RESOLVED,
            createdAt = "2026-06-19 07:10",
            assignedTo = "Rosa Fernandez",
            acknowledgedBy = "Rosa Fernandez",
            acknowledgedAt = "2026-06-19 07:15"
        )
    )

    val incidents = listOf(
        Incident(
            incidentId = "inc-001",
            code = "INC-001",
            title = "PCR freezer deviation",
            description = "Temperature deviation requires quarantine validation.",
            alertId = "alert-001",
            equipmentId = "asset-002",
            severity = Severity.CRITICAL,
            status = IncidentStatus.OPEN,
            assignedTo = "Carlos Mendoza",
            openedAt = "2026-06-19 03:12",
            dueDate = "2026-06-20",
            evidenceCount = 1,
            timeline = listOf(
                IncidentEvent("2026-06-19 03:12", "Incident opened from alert alert-001", "System")
            )
        ),
        Incident(
            incidentId = "inc-002",
            code = "INC-002",
            title = "Insulin humidity deviation",
            description = "Humidity issue detected in insulin storage.",
            alertId = "alert-002",
            equipmentId = "asset-008",
            severity = Severity.WARNING,
            status = IncidentStatus.INVESTIGATING,
            assignedTo = "Rosa Fernandez",
            openedAt = "2026-06-19 04:12",
            dueDate = "2026-06-21",
            evidenceCount = 1,
            timeline = listOf(
                IncidentEvent("2026-06-19 04:12", "Incident opened from alert alert-002", "System"),
                IncidentEvent("2026-06-19 04:30", "Status changed to Investigating", "Rosa Fernandez")
            )
        ),
        Incident(
            incidentId = "inc-003",
            code = "INC-003",
            title = "Cabinet access anomaly",
            description = "Controlled drugs cabinet door requires audit.",
            alertId = "alert-003",
            equipmentId = "asset-009",
            severity = Severity.CRITICAL,
            status = IncidentStatus.INVESTIGATING,
            assignedTo = "Gabriel Paredes",
            openedAt = "2026-06-19 05:12",
            dueDate = "2026-06-20",
            evidenceCount = 2,
            correctiveActions = listOf(
                CorrectiveAction(
                    actionId = "ACT-001",
                    description = "Cabinet door closed manually and access log requested to security.",
                    performedBy = "Gabriel Paredes",
                    performedAt = "2026-06-19 05:25"
                )
            ),
            timeline = listOf(
                IncidentEvent("2026-06-19 05:12", "Incident opened from alert alert-003", "System"),
                IncidentEvent("2026-06-19 05:18", "Status changed to Investigating", "Gabriel Paredes"),
                IncidentEvent("2026-06-19 05:25", "Corrective action registered", "Gabriel Paredes")
            )
        ),
        Incident(
            incidentId = "inc-004",
            code = "INC-004",
            title = "North incubator maintenance",
            description = "Vibration values require inspection.",
            alertId = "alert-004",
            equipmentId = "asset-006",
            severity = Severity.WARNING,
            status = IncidentStatus.OPEN,
            assignedTo = "Patricia Rivas",
            openedAt = "2026-06-19 06:12",
            dueDate = "2026-06-22",
            evidenceCount = 0,
            timeline = listOf(
                IncidentEvent("2026-06-19 06:12", "Incident opened from alert alert-004", "System")
            )
        )
    )
}
