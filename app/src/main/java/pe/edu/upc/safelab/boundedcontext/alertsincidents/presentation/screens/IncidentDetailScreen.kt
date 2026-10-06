package pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.alertsincidents.data.repository.AlertsIncidentsRepository
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.IncidentStatus
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.AlertsHeader
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.AlertsSectionCard
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.DetailLine
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.FieldLabel
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.OptionChips
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.ResultMessage
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.SecondaryButton
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.StatusChip
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.SubmitButton
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.TimelineItem
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.alertStatusColor
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.incidentStatusColor
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.severityColor
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess

@Composable
fun IncidentDetailScreen(
    incidentId: String? = null,
    onBack: () -> Unit = {},
    onOpenAlert: (String) -> Unit = {}
) {
    val repository = AlertsIncidentsRepository
    val incident = repository.findIncident(incidentId)
    var actionDescription by rememberSaveable(incidentId) { mutableStateOf("") }
    var performedBy by rememberSaveable(incidentId) { mutableStateOf<String?>(null) }
    var message by rememberSaveable(incidentId) { mutableStateOf<String?>(null) }
    var isError by rememberSaveable(incidentId) { mutableStateOf(false) }

    fun show(result: Result<*>, successMessage: String) {
        result
            .onSuccess { message = successMessage; isError = false }
            .onFailure { message = it.message; isError = true }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AlertsHeader(
                eyebrow = incident?.code ?: "Incident",
                title = incident?.title ?: "Incident detail",
                description = incident?.description ?: "Detail of an incident opened from an alert.",
                onBack = onBack
            )
        }

        if (incident == null) {
            item {
                NoDataState(
                    title = "Incident not found",
                    message = "The requested incident cannot be found or is no longer available."
                )
            }
            return@LazyColumn
        }

        val equipment = repository.findEquipment(incident.equipmentId)
        val alert = repository.findAlert(incident.alertId)

        item {
            AlertsSectionCard(eyebrow = "Summary", title = "Incident information") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(incident.severity.label, severityColor(incident.severity))
                    StatusChip(incident.status.label, incidentStatusColor(incident.status))
                }
                DetailLine("Equipment", equipment?.name ?: incident.equipmentId)
                DetailLine("Location", equipment?.location ?: "-")
                DetailLine("Assigned to", incident.assignedTo)
                DetailLine("Opened at", incident.openedAt)
                DetailLine("Due date", incident.dueDate)
                DetailLine("Closed at", incident.closedAt ?: "-")
                DetailLine("Evidence files", incident.evidenceCount.toString())
            }
        }

        if (alert != null) {
            item {
                AlertsSectionCard(eyebrow = "Origin", title = "Related alert") {
                    DetailLine("Alert", alert.title)
                    DetailLine("Recorded value", alert.value, severityColor(alert.severity))
                    DetailLine("Allowed range", alert.allowedRange)
                    DetailLine("Alert status", alert.status.label, alertStatusColor(alert.status))
                    SecondaryButton(text = "View alert ${alert.alertId}", onClick = { onOpenAlert(alert.alertId) })
                }
            }
        }

        item {
            AlertsSectionCard(eyebrow = "Workflow", title = "Status") {
                when (incident.status) {
                    IncidentStatus.OPEN -> SubmitButton(
                        text = "Start investigation",
                        onClick = { show(repository.startInvestigation(incident.incidentId), "Investigation started.") }
                    )

                    IncidentStatus.INVESTIGATING -> SubmitButton(
                        text = "Mark as resolved",
                        color = SafeLabSuccess,
                        onClick = { show(repository.resolveIncident(incident.incidentId), "Incident marked as resolved.") }
                    )

                    IncidentStatus.RESOLVED -> SubmitButton(
                        text = "Close incident",
                        onClick = { show(repository.closeIncident(incident.incidentId), "Incident closed.") }
                    )

                    IncidentStatus.CLOSED -> Text(
                        text = "This incident is closed and is kept in the history for audit purposes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (incident.status == IncidentStatus.OPEN) {
                    SecondaryButton(
                        text = "Mark as resolved",
                        onClick = { show(repository.resolveIncident(incident.incidentId), "Incident marked as resolved.") }
                    )
                }
            }
        }

        item {
            AlertsSectionCard(eyebrow = "Response", title = "Corrective actions") {
                if (incident.correctiveActions.isEmpty()) {
                    Text(
                        text = "No corrective actions registered yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    incident.correctiveActions.forEach { action ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = action.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${action.actionId} · ${action.performedBy} · ${action.performedAt}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (incident.status != IncidentStatus.CLOSED) {
                    OutlinedTextField(
                        value = actionDescription,
                        onValueChange = { actionDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        label = { Text("Corrective action *") },
                        placeholder = { Text("e.g. Batch moved to backup freezer and quarantined.") },
                        shape = RoundedCornerShape(14.dp)
                    )
                    FieldLabel("Performed by *")
                    OptionChips(
                        options = repository.teamMembers.map { it to it },
                        selected = performedBy,
                        onSelected = { performedBy = it }
                    )
                    SubmitButton(
                        text = "Register corrective action",
                        onClick = {
                            repository.registerCorrectiveAction(incident.incidentId, actionDescription, performedBy)
                                .onSuccess {
                                    message = "Corrective action registered."
                                    isError = false
                                    actionDescription = ""
                                    performedBy = null
                                }
                                .onFailure {
                                    message = it.message
                                    isError = true
                                }
                        }
                    )
                }
            }
        }

        item {
            ResultMessage(message = message, isError = isError)
        }

        item {
            AlertsSectionCard(eyebrow = "History", title = "Timeline") {
                incident.timeline.forEachIndexed { index, event ->
                    TimelineItem(event = event, isLast = index == incident.timeline.lastIndex)
                }
            }
        }
    }
}
