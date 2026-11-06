package pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.alertsincidents.data.repository.AlertsIncidentsRepository
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.AlertStatus
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.Severity
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.AlertCard
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.AlertsHeader
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.AlertsSectionCard
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.FieldLabel
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.MetricTile
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.OptionChips
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.SecondaryButton
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.StatusChip
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.severityColor
import pe.edu.upc.safelab.ui.theme.SafeLabBlue
import pe.edu.upc.safelab.common.LocalAppLanguage
import pe.edu.upc.safelab.common.tr
import pe.edu.upc.safelab.ui.theme.SafeLabDanger
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

private const val SORT_SEVERITY = "severity"
private const val SORT_NEWEST = "newest"

@Composable
fun AlertsScreen(
    onOpenAlert: (String) -> Unit = {},
    onOpenRules: () -> Unit = {},
    onOpenIncidents: () -> Unit = {}
) {
    val language = LocalAppLanguage.current
    val repository = AlertsIncidentsRepository
    var severityName by rememberSaveable { mutableStateOf<String?>(null) }
    var statusName by rememberSaveable { mutableStateOf<String?>(null) }
    var sort by rememberSaveable { mutableStateOf(SORT_SEVERITY) }
    var query by rememberSaveable { mutableStateOf("") }

    val severity = Severity.entries.firstOrNull { it.name == severityName }
    val status = AlertStatus.entries.firstOrNull { it.name == statusName }
    val visibleAlerts = repository.alerts(severity, status, query, sortBySeverity = sort == SORT_SEVERITY)
    val affectedEquipment = repository.equipmentWithActiveAlerts()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AlertsHeader(
                eyebrow = tr(language, "Alerts & incidents", "Alertas e incidentes"),
                title = tr(language, "Alerts", "Alertas"),
                description = tr(language, "Alerts generated when a monitored value leaves the limits configured for its equipment.", "Revisa alertas activas y su prioridad.")
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(text = tr(language, "Alert limits", "Límites de alerta"), onClick = onOpenRules, modifier = Modifier.weight(1f))
                SecondaryButton(text = tr(language, "Incident history", "Historial de incidentes"), onClick = onOpenIncidents, modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = tr(language, "Critical active", "Críticas activas"),
                    value = repository.criticalActiveAlerts().size.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabDanger
                )
                MetricTile(
                    label = tr(language, "Not acknowledged", "Sin reconocer"),
                    value = repository.pendingAcknowledgementCount().toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabWarning
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = tr(language, "Total alerts", "Total alertas"),
                    value = repository.alerts.size.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabBlue
                )
                MetricTile(
                    label = tr(language, "Resolved", "Resueltas"),
                    value = repository.alerts.count { it.status == AlertStatus.RESOLVED }.toString(),
                    modifier = Modifier.weight(1f),
                    accent = SafeLabSuccess
                )
            }
        }

        item {
            AlertsSectionCard(eyebrow = tr(language, "Focus", "Enfoque"), title = tr(language, "Equipment with active alerts", "Equipos con alertas activas")) {
                if (affectedEquipment.isEmpty()) {
                    NoDataState(
                        title = tr(language, "No affected equipment", "No hay equipos afectados"),
                        message = tr(language, "No registered equipment has active alerts.", "No hay equipos registrados con alertas activas.")
                    )
                } else {
                    affectedEquipment.forEach { (equipment, equipmentAlerts) ->
                        val topSeverity = equipmentAlerts.maxBy { it.severity.ordinal }.severity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = equipment.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tr(language, "${equipmentAlerts.size} active", "${equipmentAlerts.size} activas"),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            StatusChip(topSeverity.label, severityColor(topSeverity))
                        }
                    }
                }
            }
        }

        item {
            AlertsSectionCard(title = tr(language, "Filters", "Filtros")) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "Search alert or equipment", "Buscar alerta o equipo")) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                FieldLabel(tr(language, "Severity", "Severidad"))
                OptionChips(
                    options = listOf<Pair<String?, String>>(null to tr(language, "All", "Todos")) +
                        Severity.entries.reversed().map { it.name to it.label },
                    selected = severityName,
                    onSelected = { severityName = it }
                )
                FieldLabel(tr(language, "Status", "Estado"))
                OptionChips(
                    options = listOf<Pair<String?, String>>(null to tr(language, "All", "Todos")) +
                        AlertStatus.entries.map { it.name to it.label },
                    selected = statusName,
                    onSelected = { statusName = it }
                )
                FieldLabel(tr(language, "Order", "Orden"))
                OptionChips(
                    options = listOf(SORT_SEVERITY to tr(language, "Most severe first", "Más severas primero"), SORT_NEWEST to tr(language, "Newest first", "Más recientes primero")),
                    selected = sort,
                    onSelected = { if (it != null) sort = it }
                )
            }
        }

        if (visibleAlerts.isEmpty()) {
            item {
                when {
                    repository.alerts.isEmpty() -> NoDataState(
                        title = tr(language, "No alerts available", "No hay alertas disponibles"),
                        message = tr(language, "No alerts have been generated yet.", "Aún no se han generado alertas.")
                    )

                    severity == Severity.CRITICAL -> NoDataState(
                        title = tr(language, "No critical alerts", "No hay alertas críticas"),
                        message = tr(language, "There are no alerts classified as critical for the selected filters.", "No hay alertas críticas para los filtros seleccionados.")
                    )

                    else -> NoDataState(
                        title = tr(language, "No alerts found", "No se encontraron alertas"),
                        message = tr(language, "No alerts match the selected filters.", "No hay alertas que coincidan con los filtros seleccionados.")
                    )
                }
            }
        } else {
            items(visibleAlerts, key = { it.alertId }) { alert ->
                AlertCard(
                    alert = alert,
                    equipmentName = repository.findEquipment(alert.equipmentId)?.name ?: alert.equipmentId,
                    onClick = { onOpenAlert(alert.alertId) }
                )
            }
        }
    }
}