package pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.audittraceability.data.repository.AuditTraceabilityRepository
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.AuditHeader
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.AuditSectionCard
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.DetailLine
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.OptionChips
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.ProgressLine
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.RecordRow
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.ResultMessage
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.SubmitButton
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.TimelineRow
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.changeRiskColor
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.commandResultColor
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.components.evidenceStatusColor
import pe.edu.upc.safelab.ui.theme.SafeLabAccent
import pe.edu.upc.safelab.ui.theme.SafeLabBlue
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

/**
 * Traceability records (BuildTraceabilityRecord): ordered events of one operation across contexts,
 * linked evidence and export of the evidence package (ExportTraceabilityEvidence).
 */
@Composable
fun TraceabilityScreen(
    correlationId: String? = null,
    onBack: () -> Unit = {}
) {
    val repository = AuditTraceabilityRepository
    val records = repository.traceabilityRecords()
    var selectedId by rememberSaveable(correlationId) {
        mutableStateOf(correlationId ?: records.firstOrNull()?.correlationId?.value)
    }
    var message by rememberSaveable(selectedId) { mutableStateOf<String?>(null) }
    var isError by rememberSaveable(selectedId) { mutableStateOf(false) }

    val record = repository.buildTraceabilityRecord(selectedId)
    val linkedEvidence = repository.evidenceOfRecord(selectedId)
    val health = repository.health

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AuditHeader(
                eyebrow = "Traceability",
                title = "Traceability records",
                description = "Every event of an operation shares one correlation id, so it can be rebuilt from start to end.",
                onBack = onBack,
                backLabel = "Audit trail"
            )
        }

        item {
            AuditSectionCard(eyebrow = "Operation", title = "Select a record") {
                OptionChips(
                    options = records.map { it.correlationId.value to it.title },
                    selected = selectedId,
                    onSelected = { if (it != null) selectedId = it }
                )
            }
        }

        if (record == null) {
            item {
                NoDataState(
                    title = "Record not found",
                    message = "There is no traceability record for the selected correlation id."
                )
            }
        } else {
            item {
                AuditSectionCard(eyebrow = record.correlationId.value, title = record.title) {
                    DetailLine("Started at", record.startedAt)
                    DetailLine(
                        "Completed at",
                        record.completedAt ?: "In progress",
                        if (record.isCompleted()) SafeLabSuccess else SafeLabWarning
                    )
                    DetailLine("Events", record.eventSequence.size.toString())
                    DetailLine("Valid correlation id", if (record.correlationId.validate()) "Yes" else "No")
                }
            }

            item {
                AuditSectionCard(eyebrow = "Sequence", title = "Events") {
                    record.eventSequence.forEachIndexed { index, event ->
                        TimelineRow(
                            title = "${event.sequence}. ${event.description}",
                            subtitle = "${event.occurredAt} · ${event.sourceContext} · ${event.reference}",
                            color = SafeLabAccent,
                            isLast = index == record.eventSequence.lastIndex
                        )
                    }
                }
            }

            item {
                AuditSectionCard(eyebrow = "Evidence", title = "Linked evidence") {
                    if (linkedEvidence.isEmpty()) {
                        NoDataState(title = "No evidence", message = "This record has no evidence files linked.")
                    } else {
                        linkedEvidence.forEach { item ->
                            RecordRow(
                                code = item.evidenceId,
                                title = item.type,
                                subtitle = "${item.owner} · ${item.linkedRecord}",
                                chipText = item.status.label,
                                chipColor = evidenceStatusColor(item.status)
                            )
                        }
                    }
                    SubmitButton(
                        text = "Export evidence package",
                        onClick = {
                            repository.exportTraceabilityEvidence(record.correlationId.value)
                                .onSuccess { message = it; isError = false }
                                .onFailure { message = it.message; isError = true }
                        }
                    )
                    ResultMessage(message = message, isError = isError)
                }
            }
        }

        item {
            AuditSectionCard(eyebrow = "Health", title = "Traceability health") {
                ProgressLine("Audit coverage", "${health.auditCoverage} %", health.auditCoverage / 100f, SafeLabSuccess)
                ProgressLine("Evidence coverage", "${health.evidenceCoverage} %", health.evidenceCoverage / 100f, SafeLabBlue)
                ProgressLine("Change integrity", "${health.changeIntegrity} %", health.changeIntegrity / 100f, SafeLabPrimary)
                ProgressLine(
                    "Remote command traceability",
                    "${health.remoteCommandTraceability} %",
                    health.remoteCommandTraceability / 100f,
                    SafeLabAccent
                )
            }
        }

        item {
            AuditSectionCard(eyebrow = "Evidence", title = "Evidence files") {
                repository.evidence.forEach { item ->
                    RecordRow(
                        code = item.evidenceId,
                        title = item.type,
                        subtitle = "${item.owner} · ${item.linkedRecord}",
                        chipText = item.status.label,
                        chipColor = evidenceStatusColor(item.status)
                    )
                }
            }
        }

        item {
            AuditSectionCard(eyebrow = "Changes", title = "Change history") {
                repository.changeHistory.forEach { change ->
                    RecordRow(
                        code = change.changeId,
                        title = change.target,
                        subtitle = "${change.description} · ${change.changedBy}",
                        chipText = "${change.risk.label} risk",
                        chipColor = changeRiskColor(change.risk)
                    )
                }
            }
        }

        item {
            AuditSectionCard(eyebrow = "Commands", title = "Remote command history") {
                repository.commandHistory.forEach { command ->
                    RecordRow(
                        code = command.commandId,
                        title = command.command,
                        subtitle = "${command.actuator} · ${command.requestedBy}",
                        chipText = command.result.label,
                        chipColor = commandResultColor(command.result)
                    )
                }
            }
        }
    }
}
