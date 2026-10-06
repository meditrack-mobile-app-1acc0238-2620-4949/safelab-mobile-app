package pe.edu.upc.safelab.boundedcontext.audittraceability.data.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import pe.edu.upc.safelab.boundedcontext.audittraceability.data.local.AuditTraceabilityMockData
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.AuditEntry
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.AuditSeverity
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.AuditStatus
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.AuditSummary
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.ChangeRecord
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.CommandRecord
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.CorrelationId
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.EvidenceItem
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.EvidenceStatus
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.TraceabilityHealth
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.TraceabilityRecord

/**
 * In-memory, append-only repository for TB1. Audit entries are never edited or deleted:
 * a review or an export is registered as a new entry. It will be replaced by a remote
 * repository (SafeLab Platform API).
 */
object AuditTraceabilityRepository {

    private const val REVIEW_TARGET = "Audit Entry"
    private const val EXPORT_TARGET = "Traceability Record"

    private val timestampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    val currentUser: String = AuditTraceabilityMockData.CURRENT_USER

    val entries: SnapshotStateList<AuditEntry> =
        mutableStateListOf(*AuditTraceabilityMockData.auditEntries.toTypedArray())

    private val records: List<TraceabilityRecord> = AuditTraceabilityMockData.traceabilityRecords

    val evidence: List<EvidenceItem> = AuditTraceabilityMockData.evidence

    val changeHistory: List<ChangeRecord> = AuditTraceabilityMockData.changeHistory

    val commandHistory: List<CommandRecord> = AuditTraceabilityMockData.commandHistory

    val health: TraceabilityHealth = AuditTraceabilityMockData.traceabilityHealth

    private fun now(): String = LocalDateTime.now().format(timestampFormat)

    // ---------- Queries ----------

    /** GetAuditTrailHandler: newest first, filtered by severity, review status and text. */
    fun auditTrail(
        severity: AuditSeverity? = null,
        status: AuditStatus? = null,
        query: String = ""
    ): List<AuditEntry> {
        val text = query.trim().lowercase()
        return entries
            .filter { severity == null || it.severity == severity }
            .filter { status == null || statusOf(it) == status }
            .filter {
                text.isEmpty() ||
                    it.actorId.lowercase().contains(text) ||
                    it.action.lowercase().contains(text) ||
                    it.targetType.lowercase().contains(text) ||
                    it.targetId.lowercase().contains(text) ||
                    it.auditId.lowercase().contains(text)
            }
            .sortedWith(compareByDescending<AuditEntry> { it.timestamp }.thenByDescending { it.auditId })
    }

    fun findEntry(auditId: String?): AuditEntry? = entries.firstOrNull { it.auditId == auditId }

    /** An entry is reviewed when it was registered as reviewed or a later review entry references it. */
    fun statusOf(entry: AuditEntry): AuditStatus =
        if (entry.status == AuditStatus.REVIEWED ||
            entries.any { it.targetType == REVIEW_TARGET && it.targetId == entry.auditId }
        ) AuditStatus.REVIEWED else AuditStatus.PENDING

    fun summary(): AuditSummary {
        val reviewed = entries.count { statusOf(it) == AuditStatus.REVIEWED }
        return AuditSummary(
            totalEntries = entries.size,
            reviewedEntries = reviewed,
            pendingEntries = entries.size - reviewed,
            criticalEvents = entries.count { it.severity == AuditSeverity.CRITICAL },
            integrityScore = AuditTraceabilityMockData.INTEGRITY_SCORE,
            retainedDays = AuditTraceabilityMockData.RETAINED_DAYS
        )
    }

    fun latestEvents(limit: Int = 4): List<AuditEntry> = auditTrail().take(limit)

    fun evidenceOf(evidenceId: String?): EvidenceItem? = evidence.firstOrNull { it.evidenceId == evidenceId }

    fun traceabilityRecords(): List<TraceabilityRecord> =
        records.map { buildTraceabilityRecord(it.correlationId.value) ?: it }
            .sortedByDescending { it.startedAt }

    /**
     * BuildTraceabilityRecordHandler: base record of the correlation id plus the audit entries
     * with the same id that are not referenced yet (appended in order).
     */
    fun buildTraceabilityRecord(correlationId: String?): TraceabilityRecord? {
        val base = records.firstOrNull { it.correlationId.value == correlationId } ?: return null
        val referenced = base.eventSequence.map { it.reference }.toSet()
        return entries
            .filter { it.correlationId == base.correlationId && it.auditId !in referenced }
            .sortedBy { it.timestamp }
            .fold(base) { record, entry ->
                record.append(entry.timestamp, "Audit & Traceability", entry.action, entry.auditId)
            }
    }

    /** Evidence linked to the audit entries of a traceability record. */
    fun evidenceOfRecord(correlationId: String?): List<EvidenceItem> =
        entries
            .filter { it.correlationId.value == correlationId }
            .mapNotNull { evidenceOf(it.evidenceId) }
            .distinct()

    // ---------- Commands (append-only) ----------

    /** AppendAuditEntryHandler. */
    fun appendAuditEntry(
        action: String,
        targetType: String,
        targetId: String,
        correlationId: CorrelationId = CorrelationId.generate(),
        severity: AuditSeverity = AuditSeverity.INFO,
        status: AuditStatus = AuditStatus.PENDING,
        evidenceId: String? = null
    ): Result<AuditEntry> {
        if (action.isBlank()) return Result.failure(IllegalArgumentException("The audit action is required."))
        if (!correlationId.validate()) {
            return Result.failure(IllegalArgumentException("The correlation id ${correlationId.value} is not valid."))
        }
        val nextNumber = (entries.maxOfOrNull { it.auditId.removePrefix("AUD-").toIntOrNull() ?: 0 } ?: 0) + 1
        val entry = AuditEntry(
            auditId = "AUD-%04d".format(nextNumber),
            actorId = currentUser,
            action = action,
            targetType = targetType,
            targetId = targetId,
            timestamp = now(),
            correlationId = correlationId,
            severity = severity,
            status = status,
            ipAddress = AuditTraceabilityMockData.CURRENT_DEVICE,
            evidenceId = evidenceId
        )
        entries.add(entry)
        return Result.success(entry)
    }

    /** Registers the review of a pending entry as a new entry with the same correlation id. */
    fun reviewEntry(auditId: String): Result<AuditEntry> {
        val entry = findEntry(auditId)
            ?: return Result.failure(IllegalArgumentException("The audit entry cannot be found."))
        if (statusOf(entry) == AuditStatus.REVIEWED) {
            return Result.failure(IllegalArgumentException("The audit entry was already reviewed."))
        }
        return appendAuditEntry(
            action = "Reviewed audit entry ${entry.auditId}",
            targetType = REVIEW_TARGET,
            targetId = entry.auditId,
            correlationId = entry.correlationId,
            status = AuditStatus.REVIEWED
        )
    }

    /**
     * ExportTraceabilityEvidenceHandler: prepares the evidence package of a record and
     * registers the export in the audit trail. Returns the package summary.
     */
    fun exportTraceabilityEvidence(correlationId: String?): Result<String> {
        val record = buildTraceabilityRecord(correlationId)
            ?: return Result.failure(IllegalArgumentException("The traceability record cannot be found."))
        val linkedEvidence = evidenceOfRecord(correlationId)
        val pending = linkedEvidence.count { it.status == EvidenceStatus.PENDING }
        return appendAuditEntry(
            action = "Exported traceability evidence",
            targetType = EXPORT_TARGET,
            targetId = record.correlationId.value,
            correlationId = record.correlationId,
            status = AuditStatus.REVIEWED
        ).map { entry ->
            "Evidence package of ${record.correlationId} prepared with ${record.eventSequence.size} events and " +
                "${linkedEvidence.size} evidence files" +
                (if (pending > 0) " ($pending pending)." else ".") +
                " Registered as ${entry.auditId}."
        }
    }
}
