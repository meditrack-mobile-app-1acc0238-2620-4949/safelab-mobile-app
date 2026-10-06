package pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.audittraceability.data.repository.AuditTraceabilityRepository
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.AuditEntry
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.AuditSeverity
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.AuditStatus
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.AuditHeader
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.AuditSectionCard
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.DetailLine
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.FieldLabel
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.MetricTile
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.OptionChips
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.ResultMessage
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.SecondaryButton
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.StatusChip
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.SubmitButton
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.TimelineRow
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.auditSeverityColor
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.auditStatusColor
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.evidenceStatusColor
import pe.edu.upc.safelab.ui.theme.SafeLabAccent
import pe.edu.upc.safelab.ui.theme.SafeLabBlue
import pe.edu.upc.safelab.ui.theme.SafeLabDanger
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

/**
 * Audit trail (GetAuditTrail): immutable record of who did what, when and on which object.
 * Reviews are appended as new entries (AppendAuditEntry).
 */
@Composable
fun AuditTrailScreen(
    onOpenTraceability: (String?) -> Unit = {}
) {
    val repository = AuditTraceabilityRepository
    var severityName by rememberSaveable { mutableStateOf<String?>(null) }
    var statusName by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var expandedAuditId by rememberSaveable { mutableStateOf<String?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) }

    val severity = AuditSeverity.entries.firstOrNull { it.name == severityName }
    val status = AuditStatus.entries.firstOrNull { it.name == statusName }
    val trail = repository.auditTrail(severity, status, query)
    val summary = repository.summary()
    val latest = repository.latestEvents()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AuditHeader(
                eyebrow = "Audit & traceability",
                title = "Audit trail",
                description = "Append-only record of the relevant actions of the platform: actor, action, affected object and time."
            )
        }

        item {
            SecondaryButton(text = "Traceability records", onClick = { onOpenTraceability(null) })
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = "Integrity score",
                    value = "${summary.integrityScore} %",
                    modifier = Modifier.weight(1f),
                    accent = SafeLabSuccess,
                    supportingText = "Retained ${summary.retainedDays} days"
                )
                MetricTile(
                    label = "Total entries",
                    value = summary.totalEntries.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabBlue
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = "Reviewed",
                    value = summary.reviewedEntries.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabAccent
                )
                MetricTile(
                    label = "Pending",
                    value = summary.pendingEntries.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabWarning
                )
                MetricTile(
                    label = "Critical",
                    value = summary.criticalEvents.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabDanger
                )
            }
        }

        item {
            AuditSectionCard(eyebrow = "Latest", title = "Latest events") {
                if (latest.isEmpty()) {
                    NoDataState(title = "No events", message = "No audit events have been registered yet.")
                } else {
                    latest.forEachIndexed { index, entry ->
                        TimelineRow(
                            title = entry.action,
                            subtitle = "${entry.timestamp} · ${entry.targetType}",
                            color = auditStatusColor(repository.statusOf(entry)),
                            isLast = index == latest.lastIndex
                        )
                    }
                }
            }
        }

        item {
            AuditSectionCard(title = "Filters") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search actor, action or record") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                FieldLabel("Severity")
                OptionChips(
                    options = listOf<Pair<String?, String>>(null to "All") +
                        AuditSeverity.entries.reversed().map { it.name to it.label },
                    selected = severityName,
                    onSelected = { severityName = it }
                )
                FieldLabel("Review status")
                OptionChips(
                    options = listOf<Pair<String?, String>>(null to "All") +
                        AuditStatus.entries.map { it.name to it.label },
                    selected = statusName,
                    onSelected = { statusName = it }
                )
            }
        }

        item {
            ResultMessage(message = message, isError = isError)
        }

        if (trail.isEmpty()) {
            item {
                NoDataState(title = "No audit entries", message = "No audit entries match the selected filters.")
            }
        } else {
            items(trail, key = { it.auditId }) { entry ->
                AuditEntryCard(
                    entry = entry,
                    expanded = expandedAuditId == entry.auditId,
                    onToggle = {
                        expandedAuditId = if (expandedAuditId == entry.auditId) null else entry.auditId
                    },
                    onReview = {
                        repository.reviewEntry(entry.auditId)
                            .onSuccess {
                                message = "Review of ${entry.auditId} registered as ${it.auditId}."
                                isError = false
                            }
                            .onFailure {
                                message = it.message
                                isError = true
                            }
                    },
                    onOpenTraceability = { onOpenTraceability(entry.correlationId.value) }
                )
            }
        }
    }
}

@Composable
private fun AuditEntryCard(
    entry: AuditEntry,
    expanded: Boolean,
    onToggle: () -> Unit,
    onReview: () -> Unit,
    onOpenTraceability: () -> Unit
) {
    val repository = AuditTraceabilityRepository
    val status = repository.statusOf(entry)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.auditId,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = entry.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = entry.action,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${entry.actorId} · ${entry.targetType}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(entry.severity.label, auditSeverityColor(entry.severity))
                StatusChip(status.label, auditStatusColor(status))
            }

            if (expanded) {
                DetailLine("Affected record", entry.targetId)
                DetailLine("Correlation id", entry.correlationId.value)
                DetailLine("IP / device", entry.ipAddress)
                val evidence = repository.evidenceOf(entry.evidenceId)
                if (evidence != null) {
                    DetailLine("Evidence", "${evidence.evidenceId} · ${evidence.type}")
                    DetailLine("Evidence status", evidence.status.label, evidenceStatusColor(evidence.status))
                } else {
                    DetailLine("Evidence", entry.evidenceId ?: "-")
                }
                if (status == AuditStatus.PENDING) {
                    SubmitButton(text = "Mark as reviewed", onClick = onReview)
                }
                SecondaryButton(text = "View traceability", onClick = onOpenTraceability)
            }
        }
    }
}
