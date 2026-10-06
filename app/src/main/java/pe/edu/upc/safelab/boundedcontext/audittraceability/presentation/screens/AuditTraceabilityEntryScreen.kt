package pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

private enum class AuditDestination {
    AUDIT_TRAIL,
    TRACEABILITY
}

/**
 * Entry point of the Audit & Traceability module.
 * Audit trail -> traceability record of a correlation id (events, evidence and export).
 */
@Composable
fun AuditTraceabilityEntryScreen() {
    var destination by rememberSaveable { mutableStateOf(AuditDestination.AUDIT_TRAIL) }
    var selectedCorrelationId by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(enabled = destination != AuditDestination.AUDIT_TRAIL) {
        destination = AuditDestination.AUDIT_TRAIL
    }

    when (destination) {
        AuditDestination.AUDIT_TRAIL -> AuditTrailScreen(
            onOpenTraceability = { correlationId ->
                selectedCorrelationId = correlationId
                destination = AuditDestination.TRACEABILITY
            }
        )

        AuditDestination.TRACEABILITY -> TraceabilityScreen(
            correlationId = selectedCorrelationId,
            onBack = { destination = AuditDestination.AUDIT_TRAIL }
        )
    }
}
