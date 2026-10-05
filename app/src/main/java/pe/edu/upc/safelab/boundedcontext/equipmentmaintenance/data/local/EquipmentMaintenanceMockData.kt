package pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.data.local

import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.ConditionIndicator
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.EquipmentCondition
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.EquipmentReference
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.EquipmentUsage
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.MaintenanceRecord
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.MaintenanceStatus
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.MaintenanceType
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.ReliabilityAssessment
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.domain.model.StabilityPoint

/**
 * Static TB1 data aligned with the SafeLab Platform API seed (assets, sensors and incidents).
 * Condition and reliability will be calculated from Sensor Monitoring data in a later milestone.
 */
object EquipmentMaintenanceMockData {

    private const val EVALUATED_AT = "2026-06-19 02:25"
    private const val PERIOD = "Last 30 days"
    private val weeks = listOf("May 29", "Jun 05", "Jun 12", "Jun 19")

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

    private fun ref(id: String): EquipmentReference = equipment.first { it.equipmentId == id }

    val conditions = listOf(
        EquipmentCondition(
            equipment = ref("asset-001"),
            evaluatedAt = EVALUATED_AT,
            indicators = listOf(
                ConditionIndicator("Temperature", "3.8 °C", "2 °C to 8 °C", withinLimits = true)
            ),
            warnings = emptyList()
        ),
        EquipmentCondition(
            equipment = ref("asset-002"),
            evaluatedAt = EVALUATED_AT,
            indicators = listOf(
                ConditionIndicator("Temperature", "9.1 °C", "2 °C to 8 °C", withinLimits = false, deviationPercent = 14)
            ),
            warnings = listOf(
                "Temperature above 8 °C for more than 30 minutes.",
                "Linked to incident INC-001 (quarantine validation)."
            )
        ),
        EquipmentCondition(
            equipment = ref("asset-003"),
            evaluatedAt = EVALUATED_AT,
            indicators = listOf(
                ConditionIndicator("Humidity", "46 %", "35 % to 60 %", withinLimits = true),
                ConditionIndicator("Temperature", "22.4 °C", "15 °C to 25 °C", withinLimits = true)
            ),
            warnings = listOf("Monthly inspection due: last check on 2026-06-16.")
        ),
        EquipmentCondition(
            equipment = ref("asset-004"),
            evaluatedAt = EVALUATED_AT,
            indicators = listOf(
                ConditionIndicator("Door", "Closed", "Closed", withinLimits = true)
            ),
            warnings = emptyList()
        ),
        EquipmentCondition(
            equipment = ref("asset-005"),
            evaluatedAt = EVALUATED_AT,
            indicators = listOf(
                ConditionIndicator("Temperature", "4.1 °C", "2 °C to 8 °C", withinLimits = true)
            ),
            warnings = emptyList()
        ),
        EquipmentCondition(
            equipment = ref("asset-006"),
            evaluatedAt = EVALUATED_AT,
            indicators = listOf(
                ConditionIndicator("Temperature", "36.6 °C", "35 °C to 37 °C", withinLimits = true),
                ConditionIndicator(
                    "Vibration", "4.8 mm/s", "Up to 3.0 mm/s",
                    withinLimits = false, deviationPercent = 60, affectsStorage = false
                )
            ),
            warnings = listOf("Abnormal vibration: possible fan bearing wear (INC-004).")
        ),
        EquipmentCondition(
            equipment = ref("asset-007"),
            evaluatedAt = EVALUATED_AT,
            indicators = listOf(
                ConditionIndicator("Temperature", "5.1 °C", "2 °C to 8 °C", withinLimits = true)
            ),
            warnings = emptyList()
        ),
        EquipmentCondition(
            equipment = ref("asset-008"),
            evaluatedAt = EVALUATED_AT,
            indicators = listOf(
                ConditionIndicator("Temperature", "5.6 °C", "2 °C to 8 °C", withinLimits = true),
                ConditionIndicator("Humidity", "64 %", "35 % to 60 %", withinLimits = false, deviationPercent = 7)
            ),
            warnings = listOf("Humidity above the limit. Validation pending (INC-002).")
        ),
        EquipmentCondition(
            equipment = ref("asset-009"),
            evaluatedAt = EVALUATED_AT,
            indicators = listOf(
                ConditionIndicator("Door", "Open", "Closed", withinLimits = false, online = false)
            ),
            warnings = listOf(
                "Door sensor offline since 02:10.",
                "Cabinet access anomaly under audit (INC-003)."
            )
        ),
        EquipmentCondition(
            equipment = ref("asset-010"),
            evaluatedAt = EVALUATED_AT,
            indicators = listOf(
                ConditionIndicator("Temperature", "4.5 °C", "2 °C to 8 °C", withinLimits = true)
            ),
            warnings = emptyList()
        )
    )

    val maintenanceRecords = listOf(
        MaintenanceRecord(
            id = "MNT-012", equipmentId = "asset-003", type = MaintenanceType.INSPECTION,
            status = MaintenanceStatus.SCHEDULED, performedAt = "2026-06-23", performedBy = "Andrea Torres",
            notes = "Monthly inspection of the reagent rack and analyzer supplies."
        ),
        MaintenanceRecord(
            id = "MNT-011", equipmentId = "asset-006", type = MaintenanceType.CORRECTIVE,
            status = MaintenanceStatus.SCHEDULED, performedAt = "2026-06-22", performedBy = "Patricia Rivas",
            notes = "Replace the fan bearing and level the incubator."
        ),
        MaintenanceRecord(
            id = "MNT-010", equipmentId = "asset-009", type = MaintenanceType.CORRECTIVE,
            status = MaintenanceStatus.IN_PROGRESS, performedAt = "2026-06-19", performedBy = "Gabriel Paredes",
            notes = "Door sensor offline. Battery and wiring check requested.",
            observations = listOf("Technician on site at 08:00.")
        ),
        MaintenanceRecord(
            id = "MNT-009", equipmentId = "asset-002", type = MaintenanceType.CORRECTIVE,
            status = MaintenanceStatus.IN_PROGRESS, performedAt = "2026-06-19", performedBy = "Carlos Mendoza",
            notes = "Door gasket replaced after the temperature deviation. 24 h stability check pending."
        ),
        MaintenanceRecord(
            id = "MNT-008", equipmentId = "asset-006", type = MaintenanceType.INSPECTION,
            status = MaintenanceStatus.COMPLETED, performedAt = "2026-06-18", performedBy = "Patricia Rivas",
            notes = "Vibration confirmed with a portable meter. Fan bearing shows wear."
        ),
        MaintenanceRecord(
            id = "MNT-007", equipmentId = "asset-008", type = MaintenanceType.INSPECTION,
            status = MaintenanceStatus.COMPLETED, performedAt = "2026-06-18", performedBy = "Rosa Fernandez",
            notes = "Desiccant replaced and door seal checked."
        ),
        MaintenanceRecord(
            id = "MNT-006", equipmentId = "asset-010", type = MaintenanceType.PREVENTIVE,
            status = MaintenanceStatus.COMPLETED, performedAt = "2026-06-18", performedBy = "Valeria Rojas",
            notes = "Quarterly preventive maintenance of compressors and evaporators."
        ),
        MaintenanceRecord(
            id = "MNT-005", equipmentId = "asset-007", type = MaintenanceType.CALIBRATION,
            status = MaintenanceStatus.COMPLETED, performedAt = "2026-06-17", performedBy = "Rosa Fernandez",
            notes = "Temperature probe calibrated against a reference thermometer (±0.2 °C)."
        ),
        MaintenanceRecord(
            id = "MNT-004", equipmentId = "asset-001", type = MaintenanceType.PREVENTIVE,
            status = MaintenanceStatus.COMPLETED, performedAt = "2026-06-17", performedBy = "Carlos Mendoza",
            notes = "Condenser cleaning and alarm test."
        ),
        MaintenanceRecord(
            id = "MNT-003", equipmentId = "asset-005", type = MaintenanceType.PREVENTIVE,
            status = MaintenanceStatus.COMPLETED, performedAt = "2026-06-15", performedBy = "Patricia Rivas",
            notes = "Door gasket cleaning and defrost cycle check."
        ),
        MaintenanceRecord(
            id = "MNT-002", equipmentId = "asset-002", type = MaintenanceType.PREVENTIVE,
            status = MaintenanceStatus.COMPLETED, performedAt = "2026-05-28", performedBy = "Carlos Mendoza",
            notes = "Thermostat check and rack cleaning."
        ),
        MaintenanceRecord(
            id = "MNT-001", equipmentId = "asset-001", type = MaintenanceType.CALIBRATION,
            status = MaintenanceStatus.COMPLETED, performedAt = "2026-05-20", performedBy = "Carlos Mendoza",
            notes = "Annual calibration of the temperature sensor."
        )
    )

    private fun weekly(vararg values: Float): List<StabilityPoint> =
        weeks.zip(values.toList()) { label, value -> StabilityPoint(label, value) }

    val reliability = listOf(
        ReliabilityAssessment("asset-001", PERIOD, 92, 0, 99.6, 720, 0.5, weekly(98f, 99f, 97f, 99f), EquipmentUsage(24.0, 18, 72)),
        ReliabilityAssessment("asset-002", PERIOD, 58, 3, 94.2, 240, 3.5, weekly(96f, 91f, 84f, 71f), EquipmentUsage(24.0, 26, 88)),
        ReliabilityAssessment("asset-003", PERIOD, 81, 1, 98.1, 480, 1.0, weekly(97f, 95f, 94f, 92f), EquipmentUsage(10.0, 12, 60)),
        ReliabilityAssessment("asset-004", PERIOD, 95, 0, 99.8, 720, 0.2, weekly(99f, 100f, 99f, 99f), EquipmentUsage(24.0, 40, 0)),
        ReliabilityAssessment("asset-005", PERIOD, 89, 0, 99.2, 720, 0.4, weekly(97f, 98f, 98f, 97f), EquipmentUsage(24.0, 15, 65)),
        ReliabilityAssessment("asset-006", PERIOD, 69, 2, 96.5, 360, 2.5, weekly(95f, 93f, 88f, 82f), EquipmentUsage(24.0, 6, 70)),
        ReliabilityAssessment("asset-007", PERIOD, 94, 0, 99.7, 720, 0.3, weekly(99f, 98f, 99f, 99f), EquipmentUsage(24.0, 22, 78)),
        ReliabilityAssessment("asset-008", PERIOD, 72, 1, 97.4, 480, 1.8, weekly(97f, 94f, 90f, 86f), EquipmentUsage(24.0, 20, 81)),
        ReliabilityAssessment("asset-009", PERIOD, 49, 4, 89.5, 180, 5.0, weekly(93f, 85f, 76f, 64f), EquipmentUsage(24.0, 34, 40)),
        ReliabilityAssessment("asset-010", PERIOD, 96, 0, 99.9, 720, 0.1, weekly(99f, 99f, 100f, 99f), EquipmentUsage(24.0, 9, 75))
    )

    val technicians = listOf(
        "Carlos Mendoza",
        "Andrea Torres",
        "Patricia Rivas",
        "Rosa Fernandez",
        "Gabriel Paredes",
        "Valeria Rojas"
    )
}
