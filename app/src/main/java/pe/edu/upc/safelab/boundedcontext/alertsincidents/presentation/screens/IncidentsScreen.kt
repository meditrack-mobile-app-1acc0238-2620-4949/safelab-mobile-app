package pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.alertsincidents.data.repository.AlertsIncidentsRepository
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.IncidentStatus
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.Severity
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.AlertsHeader
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.AlertsSectionCard
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.FieldLabel
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.IncidentCard
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.MetricTile
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.OptionChips
import pe.edu.upc.safelab.ui.theme.SafeLabBlue
import pe.edu.upc.safelab.ui.theme.SafeLabDanger
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

@Composable
fun IncidentsScreen(
    onBack: () -> Unit = {},
    onOpenIncident: (String) -> Unit = {}
) {
    val repository = AlertsIncidentsRepository
    var statusName by rememberSaveable { mutableStateOf<String?>(null) }
    var severityName by rememberSaveable { mutableStateOf<String?>(null) }

    val status = IncidentStatus.entries.firstOrNull { it.name == statusName }
    val severity = Severity.entries.firstOrNull { it.name == severityName }
    val history = repository.incidentHistory(status, severity)
    val all = repository.incidents

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AlertsHeader(
                eyebrow = "Incident history",
                title = "Incidents",
                description = "Incidents opened from alerts, with their status, responsible and corrective actions.",
                onBack = onBack
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = "Total",
                    value = all.size.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabPrimary
                )
                MetricTile(
                    label = "Open",
                    value = all.count { it.status == IncidentStatus.OPEN }.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabDanger
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = "Investigating",
                    value = all.count { it.status == IncidentStatus.INVESTIGATING }.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabWarning
                )
                MetricTile(
                    label = "Critical",
                    value = all.count { it.severity == Severity.CRITICAL }.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabBlue
                )
            }
        }

        item {
            AlertsSectionCard(title = "Filters") {
                FieldLabel("Status")
                OptionChips(
                    options = listOf<Pair<String?, String>>(null to "All") +
                        IncidentStatus.entries.map { it.name to it.label },
                    selected = statusName,
                    onSelected = { statusName = it }
                )
                FieldLabel("Severity")
                OptionChips(
                    options = listOf<Pair<String?, String>>(null to "All") +
                        Severity.entries.reversed().map { it.name to it.label },
                    selected = severityName,
                    onSelected = { severityName = it }
                )
            }
        }

        if (history.isEmpty()) {
            item {
                if (all.isEmpty()) {
                    NoDataState(
                        title = "No incident history",
                        message = "No alerts or incidents have been registered yet."
                    )
                } else {
                    NoDataState(
                        title = "No incidents found",
                        message = "No incidents match the selected filters."
                    )
                }
            }
        } else {
            items(history, key = { it.incidentId }) { incident ->
                IncidentCard(
                    incident = incident,
                    equipmentName = repository.findEquipment(incident.equipmentId)?.name ?: incident.equipmentId,
                    onClick = { onOpenIncident(incident.incidentId) }
                )
            }
        }
    }
}
