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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.alertsincidents.data.repository.AlertsIncidentsRepository
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.AlertRule
import pe.edu.upc.safelab.boundedcontext.alertsincidents.domain.model.Severity
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.AlertsHeader
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.AlertsSectionCard
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.DetailLine
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.FieldLabel
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.NoDataState
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.OptionChips
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.ResultMessage
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.SecondaryButton
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.SubmitButton
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.components.severityColor

@Composable
fun AlertRulesScreen(
    onBack: () -> Unit = {},
    onOpenAlert: (String) -> Unit = {}
) {
    val repository = AlertsIncidentsRepository
    var selectedEquipmentId by rememberSaveable { mutableStateOf<String?>(repository.equipment.first().equipmentId) }

    val equipment = repository.findEquipment(selectedEquipmentId)
    val equipmentRules = repository.rulesOf(selectedEquipmentId)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AlertsHeader(
                eyebrow = "Alert limits",
                title = "Limits per equipment",
                description = "Define the allowed range of each monitored equipment. An alert is generated when a reading leaves it.",
                onBack = onBack
            )
        }

        item {
            AlertsSectionCard(eyebrow = "Step 1", title = "Equipment") {
                OptionChips(
                    options = repository.equipment.map { it.equipmentId to it.name },
                    selected = selectedEquipmentId,
                    onSelected = { if (it != null) selectedEquipmentId = it }
                )
                if (equipment != null) {
                    DetailLine("Identifier", equipment.identifier)
                    DetailLine("Location", equipment.location)
                }
            }
        }

        if (equipmentRules.isEmpty()) {
            item {
                NoDataState(
                    title = "No configurable limits",
                    message = "This equipment only reports open / closed door states, so it has no numeric limits."
                )
            }
        } else {
            items(equipmentRules, key = { it.ruleId }) { rule ->
                RuleEditor(rule = rule, onOpenAlert = onOpenAlert)
            }
        }
    }
}

@Composable
private fun RuleEditor(rule: AlertRule, onOpenAlert: (String) -> Unit) {
    val repository = AlertsIncidentsRepository
    var min by rememberSaveable(rule.ruleId) { mutableStateOf(AlertRule.format(rule.min)) }
    var max by rememberSaveable(rule.ruleId) { mutableStateOf(AlertRule.format(rule.max)) }
    var severityName by rememberSaveable(rule.ruleId) { mutableStateOf<String?>(rule.severity.name) }
    var reading by rememberSaveable(rule.ruleId) { mutableStateOf("") }
    var message by rememberSaveable(rule.ruleId) { mutableStateOf<String?>(null) }
    var isError by rememberSaveable(rule.ruleId) { mutableStateOf(false) }
    var createdAlertId by rememberSaveable(rule.ruleId) { mutableStateOf<String?>(null) }

    AlertsSectionCard(eyebrow = rule.ruleId, title = rule.variable.label) {
        DetailLine("Current range", rule.allowedRange)
        DetailLine("Alert severity", rule.severity.label, severityColor(rule.severity))

        FieldLabel("New limits (${rule.variable.unit})")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = min,
                onValueChange = { min = it },
                modifier = Modifier.weight(1f),
                label = { Text("Minimum *") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = max,
                onValueChange = { max = it },
                modifier = Modifier.weight(1f),
                label = { Text("Maximum *") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }
        FieldLabel("Severity when the range is exceeded *")
        OptionChips(
            options = Severity.entries.reversed().map { it.name to it.label },
            selected = severityName,
            onSelected = { severityName = it }
        )
        SubmitButton(
            text = "Save limits",
            onClick = {
                repository.changeThresholds(
                    ruleId = rule.ruleId,
                    min = min,
                    max = max,
                    severity = Severity.entries.firstOrNull { it.name == severityName }
                )
                    .onSuccess {
                        message = "Limits saved: ${it.allowedRange}."
                        isError = false
                        createdAlertId = null
                    }
                    .onFailure {
                        message = it.message
                        isError = true
                    }
            }
        )

        FieldLabel("Check a reading")
        OutlinedTextField(
            value = reading,
            onValueChange = { reading = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Reading (${rule.variable.unit})") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        SecondaryButton(
            text = "Evaluate reading",
            onClick = {
                repository.evaluateReading(rule.ruleId, reading)
                    .onSuccess { alert ->
                        isError = false
                        createdAlertId = alert?.alertId
                        message = if (alert == null) "The reading is inside the allowed range. No alert was generated."
                        else "${alert.severity.label} alert ${alert.alertId} generated."
                    }
                    .onFailure {
                        message = it.message
                        isError = true
                        createdAlertId = null
                    }
            }
        )

        ResultMessage(message = message, isError = isError)

        val alertId = createdAlertId
        if (alertId != null) {
            SecondaryButton(text = "View alert $alertId", onClick = { onOpenAlert(alertId) })
        }
    }
}
