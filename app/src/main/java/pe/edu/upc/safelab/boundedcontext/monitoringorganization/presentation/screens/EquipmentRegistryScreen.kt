package pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.data.local.MonitoringOrganizationMockData
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.data.repository.MonitoringOrganizationRepository
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
 * US05 - Register equipment
 * US06 - View equipment list
 * US07 - Assign equipment to an area
 * US08 - Search equipment by name
 */
@Composable
fun EquipmentRegistryScreen(
    areaId: String? = null,
    onBack: () -> Unit = {}
) {
    val repository = MonitoringOrganizationRepository
    var filterAreaId by rememberSaveable(areaId) { mutableStateOf(areaId) }
    var query by rememberSaveable { mutableStateOf("") }
    var showForm by rememberSaveable { mutableStateOf(false) }
    var assigningId by rememberSaveable { mutableStateOf<String?>(null) }

    val area = repository.findArea(filterAreaId)
    val results = repository.searchEquipment(query, filterAreaId)
    val areaOptions = listOf<Pair<String?, String>>(null to "All areas") +
        repository.areas.map { it.id to it.name }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OrganizationHeader(
                eyebrow = area?.let { repository.findSite(it.siteId)?.name ?: it.name } ?: "Equipment registry",
                title = if (area != null) "Equipment in ${area.name}" else "Monitored equipment",
                description = "Freezers, refrigerators, incubators and cabinets identified by their equipment code. Search by name or filter by storage area.",
                onBack = onBack,
                backLabel = if (areaId != null) "Back to areas" else "Back to sites"
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search equipment by name") },
                placeholder = { Text("e.g. Freezer") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        item {
            ChoiceChips(
                options = areaOptions,
                selected = filterAreaId,
                onSelected = { filterAreaId = it }
            )
        }

        item {
            OutlinedButton(onClick = { showForm = !showForm }) {
                Text(if (showForm) "Close form" else "+ Register equipment", fontWeight = FontWeight.Bold)
            }
        }

        if (showForm) {
            item {
                RegisterEquipmentForm(defaultAreaId = filterAreaId)
            }
        }

        item {
            Text(
                text = "${results.size} equipment found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (results.isEmpty()) {
            item {
                if (query.isNotBlank()) {
                    EmptyState(
                        title = "No matching equipment",
                        message = "No equipment name contains \"${query.trim()}\". Check the spelling or clear the area filter."
                    )
                } else {
                    EmptyState(
                        title = "No equipment available",
                        message = "Register the first equipment of this area so it can be monitored."
                    )
                }
            }
        } else {
            items(results, key = { it.id }) { item ->
                OrganizationListCard(
                    symbol = "⚙",
                    title = item.name,
                    subtitle = "${item.identifier.value} · ${item.type}",
                    statusText = item.status.label,
                    statusColor = equipmentStatusColor(item.status),
                    onClick = null
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoLine("Location", repository.locationOf(item))
                        InfoLine("Responsible", item.responsible)
                        InfoLine("Last inspection", item.lastInspection)
                        TextButton(onClick = { assigningId = if (assigningId == item.id) null else item.id }) {
                            Text(
                                text = if (item.areaId == null) "Assign to an area" else "Change area",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (assigningId == item.id) {
                            ChoiceChips(
                                options = repository.areas.map { it.id to it.name },
                                selected = item.areaId,
                                onSelected = { selectedArea ->
                                    if (selectedArea != null) {
                                        repository.assignToArea(item.id, selectedArea)
                                        assigningId = null
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterEquipmentForm(defaultAreaId: String?) {
    val repository = MonitoringOrganizationRepository
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf<String?>(null) }
    var identifier by rememberSaveable { mutableStateOf("") }
    var areaId by rememberSaveable(defaultAreaId) { mutableStateOf(defaultAreaId) }
    var responsible by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) }

    OrganizationSectionCard(eyebrow = "New equipment", title = "Register equipment") {
        FormTextField(name, { name = it }, "Equipment name *", "e.g. Vaccine Fridge B")
        Text(
            text = "Equipment type *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ChoiceChips(
            options = MonitoringOrganizationMockData.equipmentTypes.map { it to it },
            selected = type,
            onSelected = { type = it }
        )
        FormTextField(identifier, { identifier = it }, "Identifier *", "e.g. EQ-CEN-005")
        Text(
            text = "Storage area (optional)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ChoiceChips(
            options = listOf<Pair<String?, String>>(null to "Not assigned") +
                repository.areas.map { it.id to it.name },
            selected = areaId,
            onSelected = { areaId = it }
        )
        FormTextField(responsible, { responsible = it }, "Responsible", "e.g. Andrea Torres")
        PrimaryActionButton(
            text = "Register equipment",
            onClick = {
                repository.registerEquipment(name, type, identifier, areaId, responsible)
                    .onSuccess { item ->
                        message = "${item.name} (${item.identifier.value}) was registered."
                        isError = false
                        name = ""
                        identifier = ""
                        responsible = ""
                        type = null
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
