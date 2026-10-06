package pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

private enum class AlertsDestination {
    ALERTS,
    ALERT_DETAIL,
    ALERT_RULES,
    INCIDENTS,
    INCIDENT_DETAIL
}

@Composable
fun AlertsIncidentsEntryScreen() {
    var destination by rememberSaveable { mutableStateOf(AlertsDestination.ALERTS) }
    var selectedAlertId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedIncidentId by rememberSaveable { mutableStateOf<String?>(null) }
    var incidentBack by rememberSaveable { mutableStateOf(AlertsDestination.INCIDENTS) }
    var alertBack by rememberSaveable { mutableStateOf(AlertsDestination.ALERTS) }

    // Alert detail <-> incident detail return to the previous screen instead of stacking a loop.
    fun openAlert(alertId: String, from: AlertsDestination) {
        val returning = from == AlertsDestination.INCIDENT_DETAIL && incidentBack == AlertsDestination.ALERT_DETAIL
        if (!returning) alertBack = from
        selectedAlertId = alertId
        destination = AlertsDestination.ALERT_DETAIL
    }

    fun openIncident(incidentId: String, from: AlertsDestination) {
        val returning = from == AlertsDestination.ALERT_DETAIL && alertBack == AlertsDestination.INCIDENT_DETAIL
        if (!returning) incidentBack = from
        selectedIncidentId = incidentId
        destination = AlertsDestination.INCIDENT_DETAIL
    }

    BackHandler(enabled = destination != AlertsDestination.ALERTS) {
        destination = when (destination) {
            AlertsDestination.ALERT_DETAIL -> alertBack
            AlertsDestination.INCIDENT_DETAIL -> incidentBack
            else -> AlertsDestination.ALERTS
        }
    }

    when (destination) {
        AlertsDestination.ALERTS -> AlertsScreen(
            onOpenAlert = { openAlert(it, AlertsDestination.ALERTS) },
            onOpenRules = { destination = AlertsDestination.ALERT_RULES },
            onOpenIncidents = { destination = AlertsDestination.INCIDENTS }
        )

        AlertsDestination.ALERT_DETAIL -> AlertDetailScreen(
            alertId = selectedAlertId,
            onBack = { destination = alertBack },
            onOpenIncident = { openIncident(it, AlertsDestination.ALERT_DETAIL) }
        )

        AlertsDestination.ALERT_RULES -> AlertRulesScreen(
            onBack = { destination = AlertsDestination.ALERTS },
            onOpenAlert = { openAlert(it, AlertsDestination.ALERT_RULES) }
        )

        AlertsDestination.INCIDENTS -> IncidentsScreen(
            onBack = { destination = AlertsDestination.ALERTS },
            onOpenIncident = { openIncident(it, AlertsDestination.INCIDENTS) }
        )

        AlertsDestination.INCIDENT_DETAIL -> IncidentDetailScreen(
            incidentId = selectedIncidentId,
            onBack = { destination = incidentBack },
            onOpenAlert = { openAlert(it, AlertsDestination.INCIDENT_DETAIL) }
        )
    }
}
