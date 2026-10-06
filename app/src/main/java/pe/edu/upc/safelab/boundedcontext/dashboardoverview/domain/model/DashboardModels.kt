package pe.edu.upc.safelab.boundedcontext.dashboardoverview.domain.model

data class DashboardMetric(
    val label: String,
    val value: String,
    val supportingText: String,
    val tone: DashboardTone
)

data class DashboardAlert(
    val title: String,
    val description: String,
    val severity: String,
    val tone: DashboardTone
)

data class FacilityPerformance(
    val name: String,
    val typeAndLocation: String,
    val score: Int
)

data class RiskSnapshotItem(
    val label: String,
    val value: String
)

data class WorkflowStep(
    val order: Int,
    val title: String,
    val value: String
)

data class TrendPoint(
    val label: String,
    val value: Float
)

enum class DashboardTone {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    INFO
}
