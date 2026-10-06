package pe.edu.upc.safelab.boundedcontext.monitoringorganization.data.local

import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.EquipmentIdentifier
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.EquipmentStatus
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.MonitoredEquipment
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.MonitoringSite
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.SiteStatus
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.StorageArea
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.StorageAreaType

/**
 * Static TB1 data aligned with the SafeLab Platform API seed (facilities and assets).
 * It will be replaced by the RESTful API (US60) in a later milestone.
 */
object MonitoringOrganizationMockData {

    val sites = listOf(
        MonitoringSite(
            id = "fac-central",
            name = "Central Clinical Laboratory",
            type = "Clinical laboratory",
            city = "Huancayo",
            address = "Av. Giráldez 455",
            manager = "Carlos Mendoza",
            status = SiteStatus.ACTIVE
        ),
        MonitoringSite(
            id = "fac-north",
            name = "North Province Clinical Lab",
            type = "Clinical laboratory",
            city = "Cajamarca",
            address = "Jr. Amalia Puga 820",
            manager = "Patricia Rivas",
            status = SiteStatus.ACTIVE
        ),
        MonitoringSite(
            id = "fac-hospital",
            name = "San Gabriel Hospital Pharmacy",
            type = "Hospital pharmacy",
            city = "Trujillo",
            address = "Av. España 1320",
            manager = "Rosa Fernandez",
            status = SiteStatus.ACTIVE
        ),
        MonitoringSite(
            id = "fac-pharma",
            name = "PharmAndina Cold Chain",
            type = "Pharmaceutical storage",
            city = "Arequipa",
            address = "Parque Industrial, Calle 3 Lt. 14",
            manager = "Valeria Rojas",
            status = SiteStatus.ACTIVE
        )
    )

    val areas = listOf(
        StorageArea("area-cen-storage", "fac-central", "Storage 1", StorageAreaType.COLD_STORAGE),
        StorageArea("area-cen-pcr", "fac-central", "PCR Room", StorageAreaType.COLD_STORAGE),
        StorageArea("area-cen-hematology", "fac-central", "Hematology", StorageAreaType.ROOM_TEMPERATURE),
        StorageArea("area-cen-samples", "fac-central", "Sample Room", StorageAreaType.CONTROLLED_ACCESS),
        StorageArea("area-nor-storage", "fac-north", "Storage", StorageAreaType.COLD_STORAGE),
        StorageArea("area-nor-culture", "fac-north", "Culture Room", StorageAreaType.INCUBATION),
        StorageArea("area-hsp-vaccines", "fac-hospital", "Vaccines", StorageAreaType.COLD_STORAGE),
        StorageArea("area-hsp-insulin", "fac-hospital", "Insulin", StorageAreaType.COLD_STORAGE),
        StorageArea("area-hsp-restricted", "fac-hospital", "Restricted Area", StorageAreaType.CONTROLLED_ACCESS),
        StorageArea("area-pha-coldchain", "fac-pharma", "Cold Chain", StorageAreaType.COLD_ROOM)
    )

    val equipment = listOf(
        MonitoredEquipment(
            id = "asset-001",
            name = "Reagent Freezer A",
            type = "Cold Storage",
            identifier = EquipmentIdentifier("EQ-CEN-001"),
            areaId = "area-cen-storage",
            responsible = "Carlos Mendoza",
            status = EquipmentStatus.COMPLIANT,
            lastInspection = "2026-06-17"
        ),
        MonitoredEquipment(
            id = "asset-002",
            name = "PCR Reagent Rack",
            type = "Reagent Storage",
            identifier = EquipmentIdentifier("EQ-CEN-002"),
            areaId = "area-cen-pcr",
            responsible = "Carlos Mendoza",
            status = EquipmentStatus.CRITICAL,
            lastInspection = "2026-06-18"
        ),
        MonitoredEquipment(
            id = "asset-003",
            name = "Hematology Analyzer Reagents",
            type = "Analyzer Supplies",
            identifier = EquipmentIdentifier("EQ-CEN-003"),
            areaId = "area-cen-hematology",
            responsible = "Andrea Torres",
            status = EquipmentStatus.WARNING,
            lastInspection = "2026-06-16"
        ),
        MonitoredEquipment(
            id = "asset-004",
            name = "Sample Storage Door",
            type = "Access Control",
            identifier = EquipmentIdentifier("EQ-CEN-004"),
            areaId = "area-cen-samples",
            responsible = "Andrea Torres",
            status = EquipmentStatus.COMPLIANT,
            lastInspection = "2026-06-18"
        ),
        MonitoredEquipment(
            id = "asset-005",
            name = "North Lab Refrigerator",
            type = "Cold Storage",
            identifier = EquipmentIdentifier("EQ-NOR-001"),
            areaId = "area-nor-storage",
            responsible = "Patricia Rivas",
            status = EquipmentStatus.COMPLIANT,
            lastInspection = "2026-06-15"
        ),
        MonitoredEquipment(
            id = "asset-006",
            name = "North Incubator",
            type = "Equipment",
            identifier = EquipmentIdentifier("EQ-NOR-002"),
            areaId = "area-nor-culture",
            responsible = "Patricia Rivas",
            status = EquipmentStatus.WARNING,
            lastInspection = "2026-06-18"
        ),
        MonitoredEquipment(
            id = "asset-007",
            name = "Vaccine Fridge",
            type = "Pharmaceutical Cold Storage",
            identifier = EquipmentIdentifier("EQ-HSP-001"),
            areaId = "area-hsp-vaccines",
            responsible = "Rosa Fernandez",
            status = EquipmentStatus.COMPLIANT,
            lastInspection = "2026-06-17"
        ),
        MonitoredEquipment(
            id = "asset-008",
            name = "Insulin Storage",
            type = "Pharmaceutical Storage",
            identifier = EquipmentIdentifier("EQ-HSP-002"),
            areaId = "area-hsp-insulin",
            responsible = "Rosa Fernandez",
            status = EquipmentStatus.WARNING,
            lastInspection = "2026-06-18"
        ),
        MonitoredEquipment(
            id = "asset-009",
            name = "Controlled Drugs Cabinet",
            type = "Restricted Storage",
            identifier = EquipmentIdentifier("EQ-HSP-003"),
            areaId = "area-hsp-restricted",
            responsible = "Gabriel Paredes",
            status = EquipmentStatus.CRITICAL,
            lastInspection = "2026-06-16"
        ),
        MonitoredEquipment(
            id = "asset-010",
            name = "PharmAndina Cold Room",
            type = "Pharmaceutical Cold Chain",
            identifier = EquipmentIdentifier("EQ-PHA-001"),
            areaId = "area-pha-coldchain",
            responsible = "Valeria Rojas",
            status = EquipmentStatus.COMPLIANT,
            lastInspection = "2026-06-18"
        )
    )

    val siteTypes = listOf(
        "Clinical laboratory",
        "Hospital pharmacy",
        "Pharmaceutical storage"
    )

    val equipmentTypes = listOf(
        "Cold Storage",
        "Reagent Storage",
        "Analyzer Supplies",
        "Access Control",
        "Pharmaceutical Storage",
        "Equipment"
    )
}
