package pe.edu.upc.safelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.safelab.shared.domain.model.AppModule
import pe.edu.upc.safelab.shared.domain.model.MobileTab

class MobileNavigationTest {
    @Test fun `bottom navigation has five destinations without a create button`() {
        assertEquals(5, MobileTab.entries.size)
        assertEquals(listOf("Home", "Sensors", "Alerts", "Equipment", "More"),
            MobileTab.entries.map { it.label("EN") })
    }

    @Test fun `mobile labels switch to Spanish`() {
        assertEquals(listOf("Inicio", "Sensores", "Alertas", "Equipos", "Más"),
            MobileTab.entries.map { it.label("ES") })
    }

    @Test fun `all seven original operational modules remain present`() {
        assertEquals(7, AppModule.entries.count { it != AppModule.IDENTITY_ACCESS })
        assertTrue(AppModule.entries.contains(AppModule.MONITORING_ORGANIZATION))
        assertTrue(AppModule.entries.contains(AppModule.REPORTING_COMPLIANCE))
        assertTrue(AppModule.entries.contains(AppModule.AUDIT_TRACEABILITY))
    }
}
