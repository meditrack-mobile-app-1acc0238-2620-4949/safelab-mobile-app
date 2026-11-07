package pe.edu.upc.safelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.safelab.boundedcontext.dashboardinsights.data.local.DashboardMockData
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.data.local.SensorMonitoringMockData
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorStatus
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.data.repository.EnvironmentalComplianceRepository
import pe.edu.upc.safelab.boundedcontext.alertsincidents.data.local.AlertsIncidentsMockData
import pe.edu.upc.safelab.boundedcontext.audittraceability.data.local.AuditTraceabilityMockData
import pe.edu.upc.safelab.common.DemoAccountIds

/** Cross-context sample fixtures must tell the same story in the demonstration. */
class MockDataConsistencyTest {
    @Test fun dashboardSensorSnapshotMatchesMonitoring() {
        val sensors = SensorMonitoringMockData.sensors
        val active = sensors.count { it.status == SensorStatus.ACTIVE }
        val offline = sensors.count { it.status == SensorStatus.DISCONNECTED }
        val metric = DashboardMockData.metrics.first { it.label == "Telemetry coverage" }
        assertEquals("${active * 100 / sensors.size}%", metric.value)
        assertTrue(metric.supportingText.contains("$active/${sensors.size}"))
        assertEquals(offline.toString(), DashboardMockData.risks.first { it.label == "Disconnected sensors" }.value)
    }

    @Test fun violationNeverPredatesItsEvaluation() {
        val repo = EnvironmentalComplianceRepository
        for (violation in repo.violations) {
            val evaluation = repo.evaluation(violation.evaluationId)!!
            assertTrue(violation.startedAt >= evaluation.observedAt)
        }
    }

    @Test fun currentDemoOperatorsMatchAuthenticatedAdministrator() {
        assertEquals(DemoAccountIds.ADMIN_DISPLAY_NAME, AlertsIncidentsMockData.CURRENT_USER)
        assertEquals(DemoAccountIds.ADMIN_DISPLAY_NAME, AuditTraceabilityMockData.CURRENT_USER)
    }
}
