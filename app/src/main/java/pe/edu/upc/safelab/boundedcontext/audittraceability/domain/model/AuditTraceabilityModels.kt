package pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model

import java.util.UUID

/**
 * Audit & Traceability bounded context.
 * Append-only audit entries of the relevant actions of every context and traceability records
 * that correlate the events of one operation (who did what, when and on which object).
 */

/** Value object that links every event of the same operation. Format: COR-XXXXXXXX (hexadecimal). */
@JvmInline
value class CorrelationId(val value: String) {

    fun validate(): Boolean = PATTERN.matches(value)

    override fun toString(): String = value

    companion object {
        private val PATTERN = Regex("^COR-[0-9A-F]{8}$")

        fun generate(): CorrelationId =
            CorrelationId("COR-" + UUID.randomUUID().toString().replace("-", "").take(8).uppercase())
    }
}

enum class AuditSeverity(val label: String) {
    INFO("Info"),
    WARNING("Warning"),
    CRITICAL("Critical")
}

enum class AuditStatus(val label: String) {
    PENDING("Pending review"),
    REVIEWED("Reviewed")
}

/** Entity: immutable entry of the audit trail. */
data class AuditEntry(
    val auditId: String,
    val actorId: String,
    val action: String,
    /** Context or object type affected (e.g. Incident History, Sensor History). */
    val targetType: String,
    val targetId: String,
    val timestamp: String,
    val correlationId: CorrelationId,
    val severity: AuditSeverity,
    val status: AuditStatus,
    val ipAddress: String,
    val evidenceId: String? = null
)

/** Reference to one event appended to a traceability record. */
data class EventReference(
    val sequence: Int,
    val occurredAt: String,
    val sourceContext: String,
    val description: String,
    val reference: String
)

/** Aggregate / read model: ordered events that share the same correlation id. */
data class TraceabilityRecord(
    val correlationId: CorrelationId,
    val title: String,
    val startedAt: String,
    val completedAt: String? = null,
    val eventSequence: List<EventReference> = emptyList()
) {
    fun isCompleted(): Boolean = completedAt != null

    /** Events are only appended; the sequence is assigned by the record. */
    fun append(occurredAt: String, sourceContext: String, description: String, reference: String): TraceabilityRecord {
        require(correlationId.validate()) { "The correlation id ${correlationId.value} is not valid." }
        val event = EventReference(
            sequence = eventSequence.size + 1,
            occurredAt = occurredAt,
            sourceContext = sourceContext,
            description = description,
            reference = reference
        )
        return copy(eventSequence = eventSequence + event)
    }

    fun complete(at: String): TraceabilityRecord = if (isCompleted()) this else copy(completedAt = at)
}

enum class EvidenceStatus(val label: String) {
    READY("Ready"),
    PENDING("Pending")
}

data class EvidenceItem(
    val evidenceId: String,
    val type: String,
    val owner: String,
    val linkedRecord: String,
    val status: EvidenceStatus
)

enum class ChangeRisk(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

data class ChangeRecord(
    val changeId: String,
    val target: String,
    val description: String,
    val changedBy: String,
    val risk: ChangeRisk
)

enum class CommandResult(val label: String) {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled")
}

data class CommandRecord(
    val commandId: String,
    val command: String,
    val actuator: String,
    val requestedBy: String,
    val result: CommandResult
)

data class TraceabilityHealth(
    val auditCoverage: Int,
    val evidenceCoverage: Int,
    val changeIntegrity: Int,
    val remoteCommandTraceability: Int
)

/** Summary of the audit trail shown in the overview. */
data class AuditSummary(
    val totalEntries: Int,
    val reviewedEntries: Int,
    val pendingEntries: Int,
    val criticalEvents: Int,
    val integrityScore: Double,
    val retainedDays: Int
)
