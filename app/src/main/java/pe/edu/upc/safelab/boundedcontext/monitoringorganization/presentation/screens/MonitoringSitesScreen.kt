package pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.data.local.MonitoringOrganizationMockData
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.data.repository.MonitoringOrganizationRepository
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.domain.model.SiteStatus
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.ChoiceChips
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.EmptyState
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.FormMessage
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.FormTextField
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.InfoLine
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.OrganizationHeader
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.OrganizationListCard
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.OrganizationSectionCard
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.PrimaryActionButton
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.components.SummaryTile
import pe.edu.upc.safelab.ui.theme.SafeLabAccent
import pe.edu.upc.safelab.ui.theme.SafeLabMuted
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

/**
 * US01 - Register monitoring site
 * US02 - View monitoring sites
 */
@Composable
fun MonitoringSitesScreen(
    onOpenSite: (String) -> Unit = {},
    onOpenEquipmentRegistry: () -> Unit = {}
) {
    val repository = MonitoringOrganizationRepository
    val sites = repository.sites
    val summary = repository.summary()

    var showForm by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OrganizationHeader(
                eyebrow = "Monitoring organization",
                title = "Monitoring sites",
                description = "Laboratories, hospital pharmacies and storage facilities monitored by SafeLab. Open a site to see its storage areas and equipment."
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryTile("Sites", summary.sites.toString(), Modifier.weight(1f), SafeLabPrimary)
                SummaryTile("Areas", summary.areas.toString(), Modifier.weight(1f), SafeLabAccent)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryTile("Equipment", summary.equipment.toString(), Modifier.weight(1f), SafeLabPrimary)
                SummaryTile("With issues", summary.equipmentWithIssues.toString(), Modifier.weight(1f), SafeLabWarning)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showForm = !showForm },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (showForm) "Close form" else "+ Register site", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onOpenEquipmentRegistry,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Equipment registry", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showForm) {
            item {
                RegisterSiteForm(onRegistered = { })
            }
        }

        item {
            Text(
                text = "Registered sites",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (sites.isEmpty()) {
            item {
                EmptyState(
                    title = "No monitoring sites yet",
                    message = "Register your first laboratory or pharmacy to start organizing the monitoring."
                )
            }
        } else {
            items(sites, key = { it.id }) { site ->
                val areas = repository.areasOf(site.id)
                val equipment = repository.equipmentOfSite(site.id)
                OrganizationListCard(
                    symbol = "⌂",
                    title = site.name,
                    subtitle = "${site.type} · ${site.location}",
                    statusText = site.status.label,
                    statusColor = if (site.status == SiteStatus.ACTIVE) SafeLabSuccess else SafeLabMuted,
                    onClick = { onOpenSite(site.id) }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoLine("Manager", site.manager)
                        InfoLine("Storage areas", areas.size.toString())
                        InfoLine("Monitored equipment", equipment.size.toString())
                        TextButton(onClick = { onOpenSite(site.id) }) {
                            Text("View storage areas →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterSiteForm(onRegistered: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf<String?>(MonitoringOrganizationMockData.siteTypes.first()) }
    var address by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var manager by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) }

    OrganizationSectionCard(eyebrow = "New site", title = "Register monitoring site") {
        FormTextField(name, { name = it }, "Site name *", "e.g. Central Clinical Laboratory")
        Text(
            text = "Site type",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ChoiceChips(
            options = MonitoringOrganizationMockData.siteTypes.map { it to it },
            selected = type,
            onSelected = { type = it }
        )
        FormTextField(address, { address = it }, "Address *", "e.g. Av. Giráldez 455")
        FormTextField(city, { city = it }, "City *", "e.g. Huancayo")
        FormTextField(manager, { manager = it }, "Site manager", "e.g. Carlos Mendoza")
        PrimaryActionButton(
            text = "Register site",
            onClick = {
                MonitoringOrganizationRepository
                    .registerSite(name, type ?: "Clinical laboratory", city, address, manager)
                    .onSuccess { site ->
                        message = "${site.name} was registered."
                        isError = false
                        name = ""; address = ""; city = ""; manager = ""
                        onRegistered()
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
