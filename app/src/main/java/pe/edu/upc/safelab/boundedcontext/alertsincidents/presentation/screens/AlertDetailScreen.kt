package pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.alertsincidents.data.repository.AlertsIncidentsRepository
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.AlertsHeader
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.AlertsSectionCard
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.DetailLine
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.FieldLabel
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.MultiOptionChips
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.ResultMessage
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.SecondaryButton
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.StatusChip
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.SubmitButton
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.alertStatusColor
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.incidentStatusColor
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.severityColor
import pe.edu.upc.safelab.ui.theme.SafeLabDanger

@Composable
fun AlertDetailScreen(
    alertId: String? = null,
    onBack: () -> Unit = {},
    onOpenIncident: (String) -> Unit = {}
) {
    val repository = AlertsIncidentsRepository
    val alert = repository.findAlert(alertId)
    var message by rememberSaveable(alertId) { mutableStateOf<String?>(null) }
    var isError by rememberSaveable(alertId) { mutableStateOf(false) }
    var selectedMembers by remember(alertId) { mutableStateOf(listOf<String>()) }

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
                eyebrow = "Alert detail",
                title = alert?.title ?: "Alert",
                description = alert?.message ?: "Detail of the alert detected by the monitoring service.",
                onBack = onBack,
                backLabel = "Back"
            )
        }

        if (alert == null) {
            item {
                NoDataState(
                    title = "Alert not found",
                    message = "The requested alert cannot be found or is no longer available."
                )
            }
            return@LazyColumn
        }

        val equipment = repository.findEquipment(alert.equipmentId)
        val incident = repository.incidentOfAlert(alert.alertId)

        item {
            AlertsSectionCard(eyebrow = alert.alertId, title = "Detected problem") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(alert.severity.label, severityColor(alert.severity))
                    StatusChip(alert.status.label, alertStatusColor(alert.status))
                }
                DetailLine("Equipment", equipment?.name ?: alert.equipmentId)
                DetailLine("Identifier", equipment?.identifier ?: "-")
                DetailLine("Location", equipment?.location ?: "-")
                DetailLine("Variable", alert.variable.label)
                DetailLine("Recorded value", alert.value, severityColor(alert.severity))
                DetailLine("Allowed range", alert.allowedRange)
                DetailLine("Sensor", alert.sensorId)
                DetailLine("Date and time", alert.createdAt)
                DetailLine("Assigned to", alert.assignedTo)
            }
        }

        item {
            AlertsSectionCard(eyebrow = "Attention", title = "Alert handling") {
                if (alert.acknowledgedBy != null) {
                    DetailLine("Attended by", alert.acknowledgedBy)
                    DetailLine("Attended at", alert.acknowledgedAt ?: "-")
                } else {
                    Text(
                        text = "This alert has not been attended yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (alert.isPendingAcknowledgement()) {
                    SubmitButton(
                        text = "Confirm attention",
                        onClick = { show(repository.acknowledgeAlert(alert.alertId), "Alert attention confirmed.") }
                    )
                }

                if (incident != null) {
                    DetailLine("Incident", "${incident.code} · ${incident.status.label}", incidentStatusColor(incident.status))
                    SecondaryButton(text = "View incident ${incident.code}", onClick = { onOpenIncident(incident.incidentId) })
                } else if (alert.isActive()) {
                    SubmitButton(
                        text = "Escalate to incident",
                        color = SafeLabDanger,
                        onClick = {
                            repository.escalateAlert(alert.alertId)
                                .onSuccess { onOpenIncident(it.incidentId) }
                                .onFailure { message = it.message; isError = true }
                        }
                    )
                }

                if (alert.isActive()) {
                    SecondaryButton(
                        text = "Mark as resolved",
                        onClick = { show(repository.resolveAlert(alert.alertId), "Alert marked as resolved.") }
                    )
                }
            }
        }

        item {
            AlertsSectionCard(eyebrow = "Team", title = "Share with the team") {
                DetailLine(
                    "Shared with",
                    if (alert.sharedWith.isEmpty()) "Nobody yet" else alert.sharedWith.joinToString(", ")
                )
                val available = repository.teamMembers.filter { it !in alert.sharedWith }
                if (available.isEmpty()) {
                    Text(
                        text = "The whole team already has access to this alert.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FieldLabel("Team members")
                    MultiOptionChips(
                        options = available.map { it to it },
                        isSelected = { it in selectedMembers },
                        onToggle = { member ->
                            if (member != null) {
                                selectedMembers = if (member in selectedMembers) selectedMembers - member
                                else selectedMembers + member
                            }
                        }
                    )
                    SubmitButton(
                        text = "Share alert",
                        onClick = {
                            show(repository.shareAlert(alert.alertId, selectedMembers), "Alert shared with the team.")
                            if (!isError) selectedMembers = emptyList()
                        }
                    )
                }
            }
        }

        item {
            ResultMessage(message = message, isError = isError)
        }
    }
}
