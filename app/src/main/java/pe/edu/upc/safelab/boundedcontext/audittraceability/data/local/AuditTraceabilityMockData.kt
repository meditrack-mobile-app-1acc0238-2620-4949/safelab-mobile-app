package pe.edu.upc.safelab.boundedcontext.audittraceability.data.local

import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.AuditEntry
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.AuditSeverity
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.AuditStatus
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.ChangeRecord
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.ChangeRisk
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.CommandRecord
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.CommandResult
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.CorrelationId
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.EventReference
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.EvidenceItem
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.EvidenceStatus
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.TraceabilityHealth
import pe.edu.upc.safelab.boundedcontext.audittraceability.domain.model.TraceabilityRecord

/**
 * Static TB1 data aligned with the SafeLab Platform API seed (auditLogs) and the audit overview of the web app.
 * It will be replaced by the remote data source in a later milestone.
 */
object AuditTraceabilityMockData {

    /** Signed-in user of the seed (currentUser). */
    const val CURRENT_USER = "Dr. Maria Lopez"
    const val CURRENT_DEVICE = "SafeLab mobile app"

    const val INTEGRITY_SCORE = 99.4
    const val RETAINED_DAYS = 365

    private val pcrDeviation = CorrelationId("COR-7F3A21C9")
    private val coolingAdjustment = CorrelationId("COR-2B8E44D0")
    private val incidentClosure = CorrelationId("COR-91C0E7A5")
    private val complianceReview = CorrelationId("COR-5D17B3F2")
    private val permissionUpdate = CorrelationId("COR-C4A9086E")

    val auditEntries = listOf(
        AuditEntry(
            auditId = "AUD-1208",
            actorId = "Dr. Maria Lopez",
            action = "Reviewed compliance evidence",
            targetType = "Compliance Traceability",
            targetId = "CMP-087",
            timestamp = "2026-06-19 09:45",
            correlationId = complianceReview,
            severity = AuditSeverity.INFO,
            status = AuditStatus.REVIEWED,
            ipAddress = "192.168.1.24",
            evidenceId = "EVD-442"
        ),
        AuditEntry(
            auditId = "AUD-1207",
            actorId = "System",
            action = "Registered temperature threshold event",
            targetType = "Sensor History",
            targetId = "sen-003",
            timestamp = "2026-06-19 09:12",
            correlationId = pcrDeviation,
            severity = AuditSeverity.WARNING,
            status = AuditStatus.PENDING,
            ipAddress = "10.0.0.12",
            evidenceId = "EVD-441"
        ),
        AuditEntry(
            auditId = "AUD-1206",
            actorId = "Lab Technician",
            action = "Executed remote cooling adjustment",
            targetType = "Remote Command History",
            targetId = "CMD-081",
            timestamp = "2026-06-19 08:54",
            correlationId = coolingAdjustment,
            severity = AuditSeverity.CRITICAL,
            status = AuditStatus.REVIEWED,
            ipAddress = "192.168.1.41",
            evidenceId = "EVD-440"
        ),
        AuditEntry(
            auditId = "AUD-1205",
            actorId = "Compliance Officer",
            action = "Approved incident closure evidence",
            targetType = "Incident History",
            targetId = "INC-025",
            timestamp = "2026-06-18 18:20",
            correlationId = incidentClosure,
            severity = AuditSeverity.INFO,
            status = AuditStatus.REVIEWED,
            ipAddress = "192.168.1.18",
            evidenceId = "EVD-439"
        ),
        AuditEntry(
            auditId = "AUD-1204",
            actorId = "SafeLab Administrator",
            action = "Updated user permission set",
            targetType = "Change History",
            targetId = "CHG-087",
            timestamp = "2026-06-18 17:36",
            correlationId = permissionUpdate,
            severity = AuditSeverity.WARNING,
            status = AuditStatus.PENDING,
            ipAddress = "192.168.1.10",
            evidenceId = "EVD-438"
        )
    )

    val traceabilityRecords = listOf(
        TraceabilityRecord(
            correlationId = pcrDeviation,
            title = "PCR freezer deviation",
            startedAt = "2026-06-19 03:10",
            eventSequence = listOf(
                EventReference(1, "2026-06-19 03:10", "Sensor Monitoring", "Reading of 9.1 °C above the 8 °C limit", "sen-003"),
                EventReference(2, "2026-06-19 03:10", "Alerts & Incident Management", "Critical alert created", "alert-001"),
                EventReference(3, "2026-06-19 03:12", "Alerts & Incident Management", "Incident opened", "INC-001"),
                EventReference(4, "2026-06-19 09:12", "Audit & Traceability", "Temperature threshold event registered", "AUD-1207")
            )
        ),
        TraceabilityRecord(
            correlationId = coolingAdjustment,
            title = "Remote cooling adjustment",
            startedAt = "2026-06-19 08:50",
            completedAt = "2026-06-19 08:55",
            eventSequence = listOf(
                EventReference(1, "2026-06-19 08:50", "Remote Control", "Cooling level adjustment requested", "CMD-081"),
                EventReference(2, "2026-06-19 08:54", "Remote Control", "Command executed on Cold Chamber A-01", "CMD-081"),
                EventReference(3, "2026-06-19 08:54", "Audit & Traceability", "Remote command execution traced", "AUD-1206"),
                EventReference(4, "2026-06-19 08:55", "Audit & Traceability", "Command execution receipt stored", "EVD-440")
            )
        ),
        TraceabilityRecord(
            correlationId = incidentClosure,
            title = "Incident closure evidence",
            startedAt = "2026-06-18 17:50",
            completedAt = "2026-06-18 18:20",
            eventSequence = listOf(
                EventReference(1, "2026-06-18 17:50", "Alerts & Incident Management", "Incident closed by the operations team", "INC-025"),
                EventReference(2, "2026-06-18 18:05", "Reporting & Compliance", "Closure note attached as evidence", "EVD-439"),
                EventReference(3, "2026-06-18 18:20", "Audit & Traceability", "Incident closure evidence approved", "AUD-1205")
            )
        ),
        TraceabilityRecord(
            correlationId = complianceReview,
            title = "Compliance evidence review",
            startedAt = "2026-06-19 09:30",
            completedAt = "2026-06-19 09:45",
            eventSequence = listOf(
                EventReference(1, "2026-06-19 09:30", "Reporting & Compliance", "Compliance report generated", "CMP-087"),
                EventReference(2, "2026-06-19 09:45", "Audit & Traceability", "Compliance evidence reviewed", "AUD-1208")
            )
        ),
        TraceabilityRecord(
            correlationId = permissionUpdate,
            title = "User permission update",
            startedAt = "2026-06-18 17:36",
            eventSequence = listOf(
                EventReference(1, "2026-06-18 17:36", "Identity & Access Management", "Supervisor permissions updated", "CHG-087"),
                EventReference(2, "2026-06-18 17:36", "Audit & Traceability", "Change appended to the audit trail", "AUD-1204")
            )
        )
    )

    val evidence = listOf(
        EvidenceItem("EVD-442", "Compliance report", "Compliance Officer", "CMP-087", EvidenceStatus.READY),
        EvidenceItem("EVD-441", "Sensor reading snapshot", "System", "SNS-204", EvidenceStatus.PENDING),
        EvidenceItem("EVD-440", "Command execution receipt", "Lab Technician", "CMD-081", EvidenceStatus.READY),
        EvidenceItem("EVD-439", "Incident closure note", "Operations Team", "INC-025", EvidenceStatus.READY),
        EvidenceItem("EVD-438", "Permission change log", "SafeLab Administrator", "CHG-087", EvidenceStatus.PENDING)
    )

    val changeHistory = listOf(
        ChangeRecord("CHG-088", "Threshold rule", "Temperature range adjusted for Cold Room A", "Compliance Officer", ChangeRisk.MEDIUM),
        ChangeRecord("CHG-087", "User role", "Supervisor permissions updated", "SafeLab Administrator", ChangeRisk.MEDIUM),
        ChangeRecord("CHG-086", "Sensor assignment", "Sensor T-204 linked to Clinical Lab 2", "Lab Technician", ChangeRisk.LOW)
    )

    val commandHistory = listOf(
        CommandRecord("CMD-081", "Adjust cooling level", "Cold Chamber A-01", "Lab Technician", CommandResult.CONFIRMED),
        CommandRecord("CMD-080", "Restart sensor gateway", "Gateway S-02", "SafeLab Administrator", CommandResult.CONFIRMED),
        CommandRecord("CMD-079", "Cancel pending actuator command", "Freezer B-02", "Dr. Maria Lopez", CommandResult.CANCELLED)
    )

    val traceabilityHealth = TraceabilityHealth(
        auditCoverage = 98,
        evidenceCoverage = 94,
        changeIntegrity = 99,
        remoteCommandTraceability = 97
    )
}
