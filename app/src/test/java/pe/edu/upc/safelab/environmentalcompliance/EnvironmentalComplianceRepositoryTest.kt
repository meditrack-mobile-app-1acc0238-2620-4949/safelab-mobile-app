package pe.edu.upc.safelab.environmentalcompliance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.data.repository.EnvironmentalComplianceRepository as Repository
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.EvaluationStatus

/** Simple unit tests: no Android environment, network or device required. */
class EnvironmentalComplianceRepositoryTest {
    @Test fun summaryReflectsLatestEvaluationOfEachRule() {
        val summary = Repository.summary()
        assertEquals(6, summary.assessedRules)
        assertEquals(4, summary.compliantRules)
        assertEquals(2, summary.violatingRules)
        assertEquals(2, summary.activeViolations)
        assertEquals(67, summary.compliancePercent)
    }

    @Test fun limitsAreInclusive() {
        val rule = Repository.rule("R-TEMP-01")!!
        assertTrue(rule.isWithinRange(2.0))
        assertTrue(rule.isWithinRange(8.0))
        assertFalse(rule.isWithinRange(8.1))
    }

    @Test fun evaluationsIdentifyEnvironmentalDeviations() {
        assertEquals(EvaluationStatus.VIOLATION, Repository.status(Repository.evaluation("E-1006")!!))
        assertEquals(EvaluationStatus.COMPLIANT, Repository.status(Repository.evaluation("E-1002")!!))
    }

    @Test fun latestEvaluationDoesNotConfuseHistoryWithCurrentSnapshot() {
        assertEquals("E-1006", Repository.latestEvaluation("R-TEMP-01")?.id)
        assertEquals(2, Repository.findRules("", onlyViolations = true).size)
    }

    @Test fun rulesAreSearchableInSpanishAndMissingRecordsAreHandled() {
        assertEquals(2, Repository.findRules("refrigerador").size)
        assertNull(Repository.rule("R-NOT-FOUND"))
        assertNull(Repository.evaluation("E-NOT-FOUND"))
    }

    @Test fun violationHistoryPreservesResolvedEvents() {
        assertEquals(2, Repository.violations.count { it.isActive })
        assertEquals(1, Repository.violations.count { !it.isActive })
        assertEquals("INC-001", Repository.violation("V-210")?.incidentReference)
    }
}
