package pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingMetricCard
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingScreenHeader
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingSectionTitle
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingTag
import pe.edu.upc.safelab.ui.theme.SafeLabDanger
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

private data class ComplianceRulePreview(
    val id: String,
    val title: String,
    val type: String,
    val evidence: String,
    val result: String
)

private val complianceRulePreviews = listOf(
    ComplianceRulePreview("temperature", "Temperature range: 2 - 8°C", "Temperature", "Required", "Violation"),
    ComplianceRulePreview("humidity", "Humidity max: 35 - 60%", "Humidity", "Required", "Violation"),
    ComplianceRulePreview("door", "Door status: closed during storage", "Door status", "Optional", "Violation"),
    ComplianceRulePreview("vibration", "Vibration range: 0 - 3mm/s", "Vibration", "Required", "Violation"),
    ComplianceRulePreview("energy", "Energy range: 0 - 25kWh", "Energy", "Optional", "Complies")
)

@Composable
fun ComplianceScreen(
    onGenerateReport: () -> Unit = {}
) {
    var searchText by rememberSaveable { mutableStateOf("") }
    val ruleStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            complianceRulePreviews.forEach { put(it.id, true) }
        }
    }

    val visibleRules = complianceRulePreviews.filter {
        it.title.contains(searchText, ignoreCase = true) ||
            it.type.contains(searchText, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ReportingScreenHeader(
                title = "Compliance",
                subtitle = "Check environmental rules against the current SafeLab sensor status.",
                actionLabel = "Generate compliance report",
                onAction = onGenerateReport
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportingMetricCard(
                    label = "Compliance",
                    value = "20%",
                    detail = "1 of 5 rules currently complies",
                    marker = "✓",
                    markerColor = SafeLabSuccess
                )
                ReportingMetricCard(
                    label = "Active violations",
                    value = "4",
                    detail = "Temperature, humidity, door and vibration",
                    marker = "!",
                    markerColor = SafeLabDanger
                )
                ReportingMetricCard(
                    label = "Rules with evidence",
                    value = "3",
                    detail = "Evidence required for validation",
                    marker = "E",
                    markerColor = SafeLabWarning
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportingSectionTitle(
                        title = "Compliance rules",
                        subtitle = "Pause or activate rules in this local preview."
                    )
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Search rules") },
                        placeholder = { Text("Temperature, humidity, evidence...") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        items(visibleRules, key = { it.id }) { rule ->
            ComplianceRuleCard(
                rule = rule,
                isActive = ruleStates[rule.id] ?: true,
                onToggle = {
                    ruleStates[rule.id] = !(ruleStates[rule.id] ?: true)
                }
            )
        }
    }
}

@Composable
private fun ComplianceRuleCard(
    rule: ComplianceRulePreview,
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = rule.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${rule.type} · Evidence ${rule.evidence.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ReportingTag(
                    text = if (isActive) rule.result else "Paused",
                    type = if (!isActive) "paused" else rule.result
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReportingTag(
                    text = if (isActive) "Active" else "Paused",
                    type = if (isActive) "active" else "paused"
                )
                OutlinedButton(
                    onClick = onToggle,
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Text(
                        text = if (isActive) "Pause" else "Activate",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
