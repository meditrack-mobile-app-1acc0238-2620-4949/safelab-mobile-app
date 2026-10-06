package pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

private enum class OrganizationDestination {
    SITES,
    AREAS,
    EQUIPMENT
}

/**
 * Entry point of the Monitoring Organization module.
 * Flow: Monitoring sites -> Storage areas of a site -> Equipment of an area.
 */
@Composable
fun MonitoringOrganizationEntryScreen() {
    var destination by rememberSaveable { mutableStateOf(OrganizationDestination.SITES) }
    var selectedSiteId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedAreaId by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(enabled = destination != OrganizationDestination.SITES) {
        destination = when (destination) {
            OrganizationDestination.EQUIPMENT ->
                if (selectedAreaId != null) OrganizationDestination.AREAS else OrganizationDestination.SITES
            else -> OrganizationDestination.SITES
        }
    }

    when (destination) {
        OrganizationDestination.SITES -> MonitoringSitesScreen(
            onOpenSite = { siteId ->
                selectedSiteId = siteId
                destination = OrganizationDestination.AREAS
            },
            onOpenEquipmentRegistry = {
                selectedAreaId = null
                destination = OrganizationDestination.EQUIPMENT
            }
        )

        OrganizationDestination.AREAS -> StorageAreasScreen(
            siteId = selectedSiteId,
            onBack = { destination = OrganizationDestination.SITES },
            onOpenArea = { areaId ->
                selectedAreaId = areaId
                destination = OrganizationDestination.EQUIPMENT
            }
        )

        OrganizationDestination.EQUIPMENT -> EquipmentRegistryScreen(
            areaId = selectedAreaId,
            onBack = {
                destination = if (selectedAreaId != null) {
                    OrganizationDestination.AREAS
                } else {
                    OrganizationDestination.SITES
                }
            }
        )
    }
}
