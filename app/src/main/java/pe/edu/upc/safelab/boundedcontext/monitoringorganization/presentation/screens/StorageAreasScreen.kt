package pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.data.repository.MonitoringOrganizationRepository
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.EquipmentStatus
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.StorageAreaType
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.ChoiceChips
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.EmptyState
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.FormMessage
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.FormTextField
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.InfoLine
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.OrganizationHeader
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.OrganizationListCard
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.OrganizationSectionCard
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.PrimaryActionButton
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.equipmentStatusColor

/**
 * US03 - Create storage area
 * US04 - View storage areas
 */
@Composable
fun StorageAreasScreen(
    siteId: String? = null,
    onBack: () -> Unit = {},
    onOpenArea: (String) -> Unit = {}
) {
    val repository = MonitoringOrganizationRepository
    var filterSiteId by rememberSaveable(siteId) { mutableStateOf(siteId) }
    var showForm by rememberSaveable { mutableStateOf(false) }

    val site = repository.findSite(filterSiteId)
    val areas = repository.areasOf(filterSiteId)
    val siteOptions = listOf<Pair<String?, String>>(null to "All sites") +
        repository.sites.map { it.id to it.name }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OrganizationHeader(
                eyebrow = site?.name ?: "All monitoring sites",
                title = "Storage areas",
                description = if (site != null) {
                    "${site.type} · ${site.location}. Each area groups the equipment that shares the same storage conditions."
                } else {
                    "Each area groups the equipment that shares the same storage conditions."
                },
                onBack = onBack,
                backLabel = "Back to sites"
            )
        }

        item {
            ChoiceChips(
                options = siteOptions,
                selected = filterSiteId,
                onSelected = { filterSiteId = it }
            )
        }

        item {
            OutlinedButton(onClick = { showForm = !showForm }) {
                Text(if (showForm) "Close form" else "+ Create storage area", fontWeight = FontWeight.Bold)
            }
        }

        if (showForm) {
            item {
                CreateAreaForm(defaultSiteId = filterSiteId)
            }
        }

        item {
            Text(
                text = "${areas.size} storage area(s)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (areas.isEmpty()) {
            item {
                EmptyState(
                    title = "No storage areas available",
                    message = "Create an area (for example, Cold storage or Incubation) to organize the equipment of this site."
                )
            }
        } else {
            items(areas, key = { it.id }) { area ->
                val equipment = repository.equipmentOfArea(area.id)
                val worstStatus = equipment.map { it.status }.maxByOrNull { statusWeight(it) }
                    ?: EquipmentStatus.NOT_MONITORED
                val areaSite = repository.findSite(area.siteId)
                OrganizationListCard(
                    symbol = areaSymbol(area.type),
                    title = area.name,
                    subtitle = "${area.type.label} · ${areaSite?.name ?: "Unknown site"}",
                    statusText = if (equipment.isEmpty()) "Empty" else worstStatus.label,
                    statusColor = equipmentStatusColor(worstStatus),
                    onClick = { onOpenArea(area.id) }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoLine("Allowed range", area.type.range)
                        InfoLine("Equipment", equipment.size.toString())
                        TextButton(onClick = { onOpenArea(area.id) }) {
                            Text("View equipment →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateAreaForm(defaultSiteId: String?) {
    val repository = MonitoringOrganizationRepository
    var siteId by rememberSaveable(defaultSiteId) { mutableStateOf(defaultSiteId) }
    var name by rememberSaveable { mutableStateOf("") }
    var typeName by rememberSaveable { mutableStateOf<String?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) }

    OrganizationSectionCard(eyebrow = "New area", title = "Create storage area") {
        Text(
            text = "Monitoring site *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ChoiceChips(
            options = repository.sites.map { it.id to it.name },
            selected = siteId,
            onSelected = { siteId = it }
        )
        FormTextField(name, { name = it }, "Area name *", "e.g. Vaccine storage")
        Text(
            text = "Area type *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ChoiceChips(
            options = StorageAreaType.entries.map { it.name to it.label },
            selected = typeName,
            onSelected = { typeName = it }
        )
        PrimaryActionButton(
            text = "Create area",
            onClick = {
                val type = StorageAreaType.entries.firstOrNull { it.name == typeName }
                repository.createArea(siteId, name, type)
                    .onSuccess { area ->
                        message = "${area.name} was created."
                        isError = false
                        name = ""
                        typeName = null
                    }
                    .onFailure { error ->
                        message = error.message
                        isError = true
                    }
            }
        )
        FormMessage(message = message, isError = isError)
    }
}

private fun statusWeight(status: EquipmentStatus): Int = when (status) {
    EquipmentStatus.CRITICAL -> 3
    EquipmentStatus.WARNING -> 2
    EquipmentStatus.COMPLIANT -> 1
    EquipmentStatus.NOT_MONITORED -> 0
}

private fun areaSymbol(type: StorageAreaType): String = when (type) {
    StorageAreaType.COLD_STORAGE, StorageAreaType.COLD_ROOM -> "❄"
    StorageAreaType.FROZEN_STORAGE -> "✱"
    StorageAreaType.ROOM_TEMPERATURE -> "◐"
    StorageAreaType.INCUBATION -> "◎"
    StorageAreaType.CONTROLLED_ACCESS -> "▣"
}
