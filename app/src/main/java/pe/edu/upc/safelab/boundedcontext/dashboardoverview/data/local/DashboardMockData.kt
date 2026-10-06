package pe.edu.upc.safelab.boundedcontext.dashboardoverview.data.local

import pe.edu.upc.safelab.boundedcontext.dashboardoverview.domain.model.DashboardAlert
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.domain.model.DashboardMetric
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.domain.model.DashboardTone
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.domain.model.FacilityPerformance
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.domain.model.RiskSnapshotItem
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.domain.model.TrendPoint
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.domain.model.WorkflowStep

object DashboardMockData {
    val metrics = listOf(
        DashboardMetric(
            label = "Health score",
            value = "51%",
            supportingText = "Weighted operational readiness",
            tone = DashboardTone.DANGER
        ),
        DashboardMetric(
            label = "Telemetry coverage",
            value = "90%",
            supportingText = "9/10 online sensors",
            tone = DashboardTone.SUCCESS
        ),
        DashboardMetric(
            label = "Compliance score",
            value = "50%",
            supportingText = "Assets meeting current rules",
            tone = DashboardTone.DANGER
        ),
        DashboardMetric(
            label = "Open alerts",
            value = "4",
            supportingText = "Active or acknowledged events",
            tone = DashboardTone.WARNING
        ),
        DashboardMetric(
            label = "Open incidents",
            value = "4",
            supportingText = "Cases requiring follow-up",
            tone = DashboardTone.DANGER
        ),
        DashboardMetric(
            label = "Reports ready",
            value = "2",
            supportingText = "Generated exports",
            tone = DashboardTone.SUCCESS
        )
    )

    val priorityAlerts = listOf(
        DashboardAlert(
            title = "PCR freezer out of range",
            description = "PCR freezer exceeded maximum temperature.",
            severity = "Critical",
            tone = DashboardTone.DANGER
        ),
        DashboardAlert(
            title = "Insulin humidity deviation",
            description = "Insulin storage requires validation.",
            severity = "Warning",
            tone = DashboardTone.WARNING
        ),
        DashboardAlert(
            title = "Cabinet door open",
            description = "Controlled drug cabinet door remains open.",
            severity = "Critical",
            tone = DashboardTone.DANGER
        ),
        DashboardAlert(
            title = "Incubator vibration warning",
            description = "North incubator has abnormal vibration readings.",
            severity = "Warning",
            tone = DashboardTone.WARNING
        )
    )

    val facilities = listOf(
        FacilityPerformance(
            name = "Central Clinical Laboratory",
            typeAndLocation = "Clinical laboratory · Huancayo",
            score = 72
        ),
        FacilityPerformance(
            name = "North Province Clinical Lab",
            typeAndLocation = "Clinical laboratory · Cajamarca",
            score = 64
        ),
        FacilityPerformance(
            name = "San Gabriel Hospital Pharmacy",
            typeAndLocation = "Hospital pharmacy · Trujillo",
            score = 58
        ),
        FacilityPerformance(
            name = "PharmAndina Cold Chain",
            typeAndLocation = "Pharmaceutical storage · Arequipa",
            score = 81
        )
    )

    val risks = listOf(
        RiskSnapshotItem("Sensor deviations", "4"),
        RiskSnapshotItem("Disconnected sensors", "1"),
        RiskSnapshotItem("Open incidents", "4"),
        RiskSnapshotItem("Reports ready", "2")
    )

    val workflow = listOf(
        WorkflowStep(1, "Sensor event", "10 records"),
        WorkflowStep(2, "Alert generated", "4 events"),
        WorkflowStep(3, "Incident opened", "4 cases"),
        WorkflowStep(4, "Audit persisted", "4 logs")
    )

    val healthTrend = listOf(
        TrendPoint("00:00", 0.48f),
        TrendPoint("04:00", 0.53f),
        TrendPoint("08:00", 0.57f),
        TrendPoint("12:00", 0.51f),
        TrendPoint("16:00", 0.55f),
        TrendPoint("20:00", 0.51f)
    )
}
