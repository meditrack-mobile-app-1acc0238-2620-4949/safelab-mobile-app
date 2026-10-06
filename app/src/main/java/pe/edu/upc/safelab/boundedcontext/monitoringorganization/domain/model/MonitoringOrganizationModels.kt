package pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model

/**
 * Monitoring Organization bounded context.
 * Physical structure of the monitoring: sites -> storage areas -> monitored equipment.
 */

data class MonitoringSite(
    val id: String,
    val name: String,
    val type: String,
    val city: String,
    val address: String,
    val manager: String,
    val status: SiteStatus
) {
    val location: String get() = "$address · $city"
}

enum class SiteStatus(val label: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive")
}

data class StorageArea(
    val id: String,
    val siteId: String,
    val name: String,
    val type: StorageAreaType
)

enum class StorageAreaType(val label: String, val range: String) {
    COLD_STORAGE("Cold storage", "2 °C to 8 °C"),
    FROZEN_STORAGE("Frozen storage", "-25 °C to -15 °C"),
    ROOM_TEMPERATURE("Room temperature", "15 °C to 25 °C"),
    INCUBATION("Incubation", "35 °C to 37 °C"),
    CONTROLLED_ACCESS("Controlled access", "Door and access control"),
    COLD_ROOM("Cold room", "2 °C to 8 °C")
}

data class MonitoredEquipment(
    val id: String,
    val name: String,
    val type: String,
    val identifier: EquipmentIdentifier,
    val areaId: String?,
    val responsible: String,
    val status: EquipmentStatus,
    val lastInspection: String
)

/** Value object: identifier printed on the equipment label (QR). Format: EQ-XXX-000. */
@JvmInline
value class EquipmentIdentifier(val value: String) {
    fun isValid(): Boolean = PATTERN.matches(value)

    companion object {
        private val PATTERN = Regex("^EQ-[A-Z]{3}-\\d{3}$")
    }
}

enum class EquipmentStatus(val label: String) {
    COMPLIANT("Compliant"),
    WARNING("Warning"),
    CRITICAL("Critical"),
    NOT_MONITORED("Not monitored")
}

/** Summary counters shown at the top of the module. */
data class OrganizationSummary(
    val sites: Int,
    val areas: Int,
    val equipment: Int,
    val equipmentWithIssues: Int
)
