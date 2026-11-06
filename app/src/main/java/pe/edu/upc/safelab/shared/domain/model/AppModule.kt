package pe.edu.upc.safelab.shared.domain.model

enum class AppModule(
    val titleEn: String,
    val titleEs: String,
    val shortLabelEn: String,
    val shortLabelEs: String,
    val branch: String,
    val symbol: String
) {
    DASHBOARD_OVERVIEW(
        titleEn = "Dashboard & Overview",
        titleEs = "Panel e indicadores",
        shortLabelEn = "Dashboard",
        shortLabelEs = "Inicio",
        branch = "feature/tb1-bc-dashboard-overview",
        symbol = "▦"
    ),
    MONITORING_ORGANIZATION(
        titleEn = "Monitoring Organization",
        titleEs = "Organización",
        shortLabelEn = "Organization",
        shortLabelEs = "Organización",
        branch = "feature/tb1-bc-monitoring-organization",
        symbol = "⌂"
    ),
    SENSOR_MONITORING(
        titleEn = "Sensor Monitoring",
        titleEs = "Sensores",
        shortLabelEn = "Sensors",
        shortLabelEs = "Sensores",
        branch = "feature/tb1-bc-sensor-monitoring",
        symbol = "⌁"
    ),
    ALERTS_INCIDENTS(
        titleEn = "Alerts & Incident Management",
        titleEs = "Alertas e incidentes",
        shortLabelEn = "Alerts",
        shortLabelEs = "Alertas",
        branch = "feature/tb1-bc-alerts-incidents",
        symbol = "!"
    ),
    EQUIPMENT_MAINTENANCE(
        titleEn = "Equipment Condition & Maintenance",
        titleEs = "Equipos",
        shortLabelEn = "Equipment",
        shortLabelEs = "Equipos",
        branch = "feature/tb1-bc-equipment-maintenance",
        symbol = "⚙"
    ),
    REPORTING_COMPLIANCE(
        titleEn = "Reporting & Analytics",
        titleEs = "Reportes y análisis",
        shortLabelEn = "Reports & Analytics",
        shortLabelEs = "Reportes y análisis",
        branch = "feature/tb1-bc-reporting-compliance",
        symbol = "▤"
    ),
    ENVIRONMENTAL_COMPLIANCE(
        titleEn = "Environmental Compliance Management",
        titleEs = "Gestión de cumplimiento ambiental",
        shortLabelEn = "Environmental compliance",
        shortLabelEs = "Cumplimiento ambiental",
        branch = "feature/av2-environmental-compliance",
        symbol = "✓"
    ),
    AUDIT_TRACEABILITY(
        titleEn = "Audit & Traceability",
        titleEs = "Auditoría y trazabilidad",
        shortLabelEn = "Audit Trail",
        shortLabelEs = "Auditoría y trazabilidad",
        branch = "feature/tb1-bc-audit-traceability",
        symbol = "↺"
    ),
    IDENTITY_ACCESS(
        titleEn = "Identity & Access Management",
        titleEs = "Acceso",
        shortLabelEn = "Identity & Access",
        shortLabelEs = "Acceso",
        branch = "feature/tb1-bc-identity-access",
        symbol = "●"
    );

    fun title(language: String) = if (language == "ES") titleEs else titleEn
    fun shortLabel(language: String) = if (language == "ES") shortLabelEs else shortLabelEn
}