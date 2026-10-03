package pe.edu.upc.safelab.shared.domain.model

enum class AppModule(
    val title: String,
    val shortLabel: String,
    val branch: String,
    val symbol: String
) {
    DASHBOARD_OVERVIEW(
        title = "Dashboard & Overview",
        shortLabel = "Dashboard",
        branch = "feature/tb1-bc-dashboard-overview",
        symbol = "▦"
    ),
    MONITORING_ORGANIZATION(
        title = "Monitoring Organization",
        shortLabel = "Organization",
        branch = "feature/tb1-bc-monitoring-organization",
        symbol = "⌂"
    ),
    SENSOR_MONITORING(
        title = "Sensor Monitoring",
        shortLabel = "Sensors",
        branch = "feature/tb1-bc-sensor-monitoring",
        symbol = "⌁"
    ),
    ALERTS_INCIDENTS(
        title = "Alerts & Incident Management",
        shortLabel = "Alerts & Incidents",
        branch = "feature/tb1-bc-alerts-incidents",
        symbol = "!"
    ),
    EQUIPMENT_MAINTENANCE(
        title = "Equipment Condition & Maintenance",
        shortLabel = "Equipment",
        branch = "feature/tb1-bc-equipment-maintenance",
        symbol = "⚙"
    ),
    REPORTING_COMPLIANCE(
        title = "Reporting & Compliance",
        shortLabel = "Reports & Compliance",
        branch = "feature/tb1-bc-reporting-compliance",
        symbol = "▤"
    ),
    AUDIT_TRACEABILITY(
        title = "Audit & Traceability",
        shortLabel = "Audit Trail",
        branch = "feature/tb1-bc-audit-traceability",
        symbol = "↺"
    ),
    IDENTITY_ACCESS(
        title = "Identity & Access Management",
        shortLabel = "Identity & Access",
        branch = "feature/tb1-bc-identity-access",
        symbol = "●"
    )
}
