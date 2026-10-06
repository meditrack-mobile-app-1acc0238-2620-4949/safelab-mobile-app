package pe.edu.upc.safelab.boundedcontext.monitoringorganization.data.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.data.local.MonitoringOrganizationMockData
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.EquipmentIdentifier
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.EquipmentStatus
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.MonitoredEquipment
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.MonitoringSite
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.OrganizationSummary
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.SiteStatus
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.StorageArea
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.StorageAreaType

/**
 * In-memory repository for TB1. Keeps the registered sites, areas and equipment
 * while the app is open. The lists are observable, so the screens update automatically.
 * It will be replaced by a remote repository that calls the SafeLab Platform API (US60).
 */
object MonitoringOrganizationRepository {

    val sites: SnapshotStateList<MonitoringSite> =
        mutableStateListOf(*MonitoringOrganizationMockData.sites.toTypedArray())

    val areas: SnapshotStateList<StorageArea> =
        mutableStateListOf(*MonitoringOrganizationMockData.areas.toTypedArray())

    val equipment: SnapshotStateList<MonitoredEquipment> =
        mutableStateListOf(*MonitoringOrganizationMockData.equipment.toTypedArray())

    // ---------- Queries ----------

    fun findSite(siteId: String?): MonitoringSite? = sites.firstOrNull { it.id == siteId }

    fun findArea(areaId: String?): StorageArea? = areas.firstOrNull { it.id == areaId }

    fun areasOf(siteId: String?): List<StorageArea> =
        if (siteId == null) areas.toList() else areas.filter { it.siteId == siteId }

    fun equipmentOfArea(areaId: String): List<MonitoredEquipment> =
        equipment.filter { it.areaId == areaId }

    fun equipmentOfSite(siteId: String): List<MonitoredEquipment> {
        val areaIds = areasOf(siteId).map { it.id }.toSet()
        return equipment.filter { it.areaId in areaIds }
    }

    /** US08: search equipment by name (case insensitive), optionally inside one area. */
    fun searchEquipment(query: String, areaId: String? = null): List<MonitoredEquipment> =
        equipment.filter { item ->
            (areaId == null || item.areaId == areaId) &&
                (query.isBlank() || item.name.contains(query.trim(), ignoreCase = true))
        }

    /** Readable location of an equipment: "Area · Site". */
    fun locationOf(item: MonitoredEquipment): String {
        val area = findArea(item.areaId) ?: return "Not assigned to an area"
        val site = findSite(area.siteId)
        return if (site == null) area.name else "${area.name} · ${site.name}"
    }

    fun summary(): OrganizationSummary = OrganizationSummary(
        sites = sites.size,
        areas = areas.size,
        equipment = equipment.size,
        equipmentWithIssues = equipment.count {
            it.status == EquipmentStatus.WARNING || it.status == EquipmentStatus.CRITICAL
        }
    )

    // ---------- Commands ----------

    /** US01: a site needs a name and a location. */
    fun registerSite(
        name: String,
        type: String,
        city: String,
        address: String,
        manager: String
    ): Result<MonitoringSite> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Enter the site name."))
        if (city.isBlank() || address.isBlank()) {
            return Result.failure(IllegalArgumentException("Enter the site location (address and city)."))
        }
        val site = MonitoringSite(
            id = "fac-${System.currentTimeMillis()}",
            name = name.trim(),
            type = type,
            city = city.trim(),
            address = address.trim(),
            manager = manager.trim().ifBlank { "Not assigned" },
            status = SiteStatus.ACTIVE
        )
        sites.add(site)
        return Result.success(site)
    }

    /** US03: an area needs a site, a name and a type. */
    fun createArea(siteId: String?, name: String, type: StorageAreaType?): Result<StorageArea> {
        if (findSite(siteId) == null) return Result.failure(IllegalArgumentException("Select the monitoring site."))
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Enter the area name."))
        if (type == null) return Result.failure(IllegalArgumentException("Select the area type."))
        val area = StorageArea(
            id = "area-${System.currentTimeMillis()}",
            siteId = siteId!!,
            name = name.trim(),
            type = type
        )
        areas.add(area)
        return Result.success(area)
    }

    /** US05 + US07: equipment needs name, type and a valid, unique identifier; the area is optional. */
    fun registerEquipment(
        name: String,
        type: String?,
        identifier: String,
        areaId: String?,
        responsible: String
    ): Result<MonitoredEquipment> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Enter the equipment name."))
        if (type.isNullOrBlank()) return Result.failure(IllegalArgumentException("Select the equipment type."))
        val id = EquipmentIdentifier(identifier.trim().uppercase())
        if (!id.isValid()) return Result.failure(IllegalArgumentException("Use the identifier format EQ-ABC-001."))
        if (equipment.any { it.identifier == id }) {
            return Result.failure(IllegalArgumentException("Identifier ${id.value} is already registered."))
        }
        if (areaId != null && findArea(areaId) == null) {
            return Result.failure(IllegalArgumentException("The selected area does not exist."))
        }
        val item = MonitoredEquipment(
            id = "asset-${System.currentTimeMillis()}",
            name = name.trim(),
            type = type,
            identifier = id,
            areaId = areaId,
            responsible = responsible.trim().ifBlank { "Not assigned" },
            status = EquipmentStatus.NOT_MONITORED,
            lastInspection = "Pending"
        )
        equipment.add(item)
        return Result.success(item)
    }

    /** US07: assign (or move) an equipment to a storage area. */
    fun assignToArea(equipmentId: String, areaId: String): Result<MonitoredEquipment> {
        val index = equipment.indexOfFirst { it.id == equipmentId }
        if (index < 0) return Result.failure(IllegalArgumentException("The equipment does not exist."))
        if (findArea(areaId) == null) return Result.failure(IllegalArgumentException("The area does not exist."))
        val updated = equipment[index].copy(areaId = areaId)
        equipment[index] = updated
        return Result.success(updated)
    }
}
