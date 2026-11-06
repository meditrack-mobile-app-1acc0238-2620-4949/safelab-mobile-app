package pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.data.repository.EnvironmentalComplianceRepository as Repo
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.EvaluationStatus
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.components.ComplianceBadge
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.components.ComplianceEvaluationRow
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.components.ComplianceHeading
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.components.ComplianceInfoRow
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.components.ComplianceMetric
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.components.CompliancePanel
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.components.ComplianceRuleRow
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.components.ComplianceSeverityBadge
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.components.formatNumber
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.components.formatSampleDate
import pe.edu.upc.safelab.common.LocalAppLanguage
import pe.edu.upc.safelab.common.tr
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary

private enum class ComplianceSection { OVERVIEW, RULES, EVALUATIONS, VIOLATIONS }
private enum class ComplianceDetail { RULE, EVALUATION, VIOLATION }

/** Module entry point. Each detail stays inside this BC and does not change the bottom navigation. */
@Composable
fun EnvironmentalComplianceEntryScreen() {
    val language = LocalAppLanguage.current
    var section by rememberSaveable { mutableStateOf(ComplianceSection.OVERVIEW.name) }
    var detailType by rememberSaveable { mutableStateOf<String?>(null) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }

    fun open(type: ComplianceDetail, id: String) { detailType = type.name; detailId = id }
    fun back() { if (detailId != null) { detailId = null; detailType = null }
        else section = ComplianceSection.OVERVIEW.name }

    BackHandler(enabled = detailId != null || section != ComplianceSection.OVERVIEW.name) { back() }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (detailId == null) {
            LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ComplianceSection.entries) { tab ->
                    val selected = section == tab.name
                    Surface(modifier = Modifier.clickable { section = tab.name },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) SafeLabPrimary else MaterialTheme.colorScheme.surfaceVariant) {
                        Text(when (tab) {
                            ComplianceSection.OVERVIEW -> tr(language, "Overview", "Resumen")
                            ComplianceSection.RULES -> tr(language, "Rules", "Reglas")
                            ComplianceSection.EVALUATIONS -> tr(language, "Evaluations", "Evaluaciones")
                            ComplianceSection.VIOLATIONS -> tr(language, "Violations", "Incumplimientos")
                        }, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            when (ComplianceSection.valueOf(section)) {
                ComplianceSection.OVERVIEW -> ComplianceOverview(
                    onRules = { section = ComplianceSection.RULES.name },
                    onViolations = { section = ComplianceSection.VIOLATIONS.name },
                    onEvaluation = { open(ComplianceDetail.EVALUATION, it) })
                ComplianceSection.RULES -> ComplianceRules(onOpenRule = { open(ComplianceDetail.RULE, it) })
                ComplianceSection.EVALUATIONS -> ComplianceEvaluations(onOpenEvaluation = { open(ComplianceDetail.EVALUATION, it) })
                ComplianceSection.VIOLATIONS -> ComplianceViolations(onOpenViolation = { open(ComplianceDetail.VIOLATION, it) })
            }
        } else {
            val type = ComplianceDetail.entries.find { it.name == detailType }
            ComplianceDetailScreen(type, detailId.orEmpty(), onBack = ::back,
                onOpenRule = { open(ComplianceDetail.RULE, it) },
                onOpenEvaluation = { open(ComplianceDetail.EVALUATION, it) })
        }
    }
}

@Composable
private fun ComplianceOverview(onRules: () -> Unit, onViolations: () -> Unit, onEvaluation: (String) -> Unit) {
    val language = LocalAppLanguage.current
    val summary = Repo.summary()
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = .18f),
                        MaterialTheme.colorScheme.surface))) .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(tr(language, "ENVIRONMENTAL COMPLIANCE", "CUMPLIMIENTO AMBIENTAL"),
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold)
                    Text(tr(language, "Cold-chain conditions", "Condiciones de cadena de frío"),
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text(tr(language, "Review how example sensor readings compare with configured preservation limits.",
                        "Revisa cómo se comparan las lecturas de ejemplo con los límites de conservación configurados."),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ComplianceBadge(tr(language, "Offline demonstration data", "Datos de demostración sin conexión"), true)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ComplianceMetric(tr(language, "Compliance", "Cumplimiento"), "${summary.compliancePercent}%",
                    tr(language, "${summary.compliantRules} of ${summary.assessedRules} rules", "${summary.compliantRules} de ${summary.assessedRules} reglas"),
                    MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                ComplianceMetric(tr(language, "Out of range", "Fuera de rango"), "${summary.violatingRules}",
                    tr(language, "Latest assessments", "Últimas evaluaciones"),
                    MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }
        }
        item {
            CompliancePanel {
                Text(tr(language, "Active violations", "Incumplimientos activos"),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(summary.activeViolations.toString(), style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black)
                Text(tr(language, "Issues requiring review in the sample records.",
                    "Eventos que requieren revisión en los registros de ejemplo."),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onViolations, shape = RoundedCornerShape(12.dp)) {
                    Text(tr(language, "View violations", "Ver incumplimientos"))
                }
            }
        }
        item {
            CompliancePanel {
                Text(tr(language, "Environmental rules", "Reglas ambientales"),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(tr(language, "Defined ranges, monitored devices and required evidence.",
                    "Rangos definidos, equipos monitoreados y evidencia requerida."),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onRules, shape = RoundedCornerShape(12.dp)) {
                    Text(tr(language, "Review rules", "Revisar reglas"))
                }
            }
        }
        item {
            Text(tr(language, "Recent evaluations", "Evaluaciones recientes"),
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(Repo.evaluations.sortedByDescending { it.observedAt }.take(3), key = { it.id }) { item ->
            ComplianceEvaluationRow(item, language) { onEvaluation(item.id) }
        }
    }
}

@Composable
private fun ComplianceRules(onOpenRule: (String) -> Unit) {
    val language = LocalAppLanguage.current
    var query by rememberSaveable { mutableStateOf("") }
    var onlyViolations by rememberSaveable { mutableStateOf(false) }
    val visible = Repo.findRules(query, onlyViolations)
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ComplianceHeading(tr(language, "Environmental rules", "Reglas ambientales"),
            tr(language, "Read-only thresholds by asset and sensor.", "Umbrales de solo lectura por activo y sensor.")) }
        item {
            OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
                label = { Text(tr(language, "Search rule or equipment", "Buscar regla o equipo")) },
                singleLine = true, shape = RoundedCornerShape(13.dp))
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterButton(tr(language, "All", "Todas"), !onlyViolations) { onlyViolations = false }
                FilterButton(tr(language, "Out of range", "Fuera de rango"), onlyViolations) { onlyViolations = true }
            }
        }
        item { Text(tr(language, "${visible.size} rules", "${visible.size} reglas"),
            style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(visible, key = { it.id }) { rule -> ComplianceRuleRow(rule, language) { onOpenRule(rule.id) } }
        if (visible.isEmpty()) item { EmptyComplianceResults() }
    }
}

@Composable
private fun ComplianceEvaluations(onOpenEvaluation: (String) -> Unit) {
    val language = LocalAppLanguage.current
    var filter by rememberSaveable { mutableStateOf("all") }
    val visible = Repo.evaluations.sortedByDescending { it.observedAt }.filter { evaluation ->
        filter == "all" || (filter == "violations" && Repo.status(evaluation) == EvaluationStatus.VIOLATION)
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ComplianceHeading(tr(language, "Compliance evaluations", "Evaluaciones de cumplimiento"),
            tr(language, "Recorded example readings compared with defined rules.",
                "Lecturas de ejemplo registradas y comparadas con las reglas definidas.")) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterButton(tr(language, "All", "Todas"), filter == "all") { filter = "all" }
                FilterButton(tr(language, "Violations", "Incumplimientos"), filter == "violations") { filter = "violations" }
            }
        }
        items(visible, key = { it.id }) { evaluation ->
            ComplianceEvaluationRow(evaluation, language) { onOpenEvaluation(evaluation.id) }
        }
        if (visible.isEmpty()) item { EmptyComplianceResults() }
    }
}

@Composable
private fun ComplianceViolations(onOpenViolation: (String) -> Unit) {
    val language = LocalAppLanguage.current
    var filter by rememberSaveable { mutableStateOf("all") }
    val visible = Repo.violations.sortedByDescending { it.startedAt }.filter { filter != "active" || it.isActive }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ComplianceHeading(tr(language, "Violation history", "Historial de incumplimientos"),
            tr(language, "Review the severity, duration and related evidence of each example event.",
                "Consulta la severidad, duración y evidencia asociada de cada evento de ejemplo.")) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterButton(tr(language, "All", "Todos"), filter == "all") { filter = "all" }
                FilterButton(tr(language, "Active", "Activos"), filter == "active") { filter = "active" }
            }
        }
        items(visible, key = { it.id }) { violation ->
            val evaluation = Repo.evaluation(violation.evaluationId)
            val rule = evaluation?.let { Repo.rule(it.ruleId) }
            CompliancePanel(modifier = Modifier.clickable { onOpenViolation(violation.id) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(violation.id, modifier = Modifier.weight(1f), fontWeight = FontWeight.Black)
                    ComplianceSeverityBadge(violation.severity, language)
                    ComplianceBadge(if (violation.isActive) tr(language, "Active", "Activo")
                        else tr(language, "Resolved", "Resuelto"), !violation.isActive)
                }
                Text(rule?.let { tr(language, it.nameEn, it.nameEs) } ?: violation.evaluationId,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(formatSampleDate(violation.startedAt, language),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(tr(language, "View violation details  ›", "Ver detalle del incumplimiento  ›"),
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium)
            }
        }
        if (visible.isEmpty()) item { EmptyComplianceResults() }
    }
}

@Composable
private fun ComplianceDetailScreen(
    type: ComplianceDetail?, id: String, onBack: () -> Unit,
    onOpenRule: (String) -> Unit, onOpenEvaluation: (String) -> Unit
) {
    val language = LocalAppLanguage.current
    val rule = when (type) {
        ComplianceDetail.RULE -> Repo.rule(id)
        ComplianceDetail.EVALUATION -> Repo.evaluation(id)?.let { Repo.rule(it.ruleId) }
        ComplianceDetail.VIOLATION -> Repo.violation(id)?.let { Repo.evaluation(it.evaluationId) }?.let { Repo.rule(it.ruleId) }
        null -> null
    }
    val evaluation = when (type) {
        ComplianceDetail.RULE -> Repo.latestEvaluation(id)
        ComplianceDetail.EVALUATION -> Repo.evaluation(id)
        ComplianceDetail.VIOLATION -> Repo.violation(id)?.let { Repo.evaluation(it.evaluationId) }
        null -> null
    }
    val violation = if (type == ComplianceDetail.VIOLATION) Repo.violation(id) else null
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(12.dp)) {
                Text(tr(language, "‹ Back to list", "‹ Volver a la lista"))
            }
        }
        if (rule == null || evaluation == null) {
            item { EmptyComplianceResults() }
        } else {
            item {
                ComplianceHeading(when (type) {
                    ComplianceDetail.RULE -> tr(language, "Rule details", "Detalle de regla")
                    ComplianceDetail.EVALUATION -> tr(language, "Evaluation details", "Detalle de evaluación")
                    else -> tr(language, "Violation details", "Detalle del incumplimiento")
                }, "${id} · ${rule.equipment}")
            }
            item {
                CompliancePanel {
                    Text(tr(language, rule.nameEn, rule.nameEs), style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black)
                    ComplianceBadge(if (Repo.status(evaluation) == EvaluationStatus.COMPLIANT)
                        tr(language, "Compliant", "Conforme") else tr(language, "Violation", "Incumplimiento"),
                        Repo.status(evaluation) == EvaluationStatus.COMPLIANT)
                    ComplianceInfoRow(tr(language, "Location", "Ubicación"), rule.area)
                    ComplianceInfoRow(tr(language, "Equipment", "Equipo"), rule.equipment)
                    ComplianceInfoRow(tr(language, "Sensor", "Sensor"), rule.sensorId)
                    ComplianceInfoRow(tr(language, "Allowed range", "Rango permitido"),
                        "${formatNumber(rule.minimum)} – ${formatNumber(rule.maximum)} ${rule.unit}")
                    ComplianceInfoRow(tr(language, "Measured value", "Valor medido"),
                        "${formatNumber(evaluation.measuredValue)} ${rule.unit}")
                    ComplianceInfoRow(tr(language, "Recorded at", "Registrado el"),
                        formatSampleDate(evaluation.observedAt, language))
                    ComplianceInfoRow(tr(language, "Evidence required", "Evidencia requerida"),
                        if (rule.evidenceRequired) tr(language, "Yes", "Sí") else tr(language, "No", "No"))
                }
            }
            item {
                CompliancePanel {
                    Text(tr(language, "Evidence and interpretation", "Evidencia e interpretación"),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(tr(language, evaluation.evidenceEn, evaluation.evidenceEs),
                        style = MaterialTheme.typography.bodyMedium)
                    Text(tr(language, rule.descriptionEn, rule.descriptionEs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (violation != null) {
                item {
                    CompliancePanel {
                        Text(tr(language, "Violation record", "Registro de incumplimiento"),
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        ComplianceSeverityBadge(violation.severity, language)
                        ComplianceInfoRow(tr(language, "Started", "Inicio"), formatSampleDate(violation.startedAt, language))
                        ComplianceInfoRow(tr(language, "Resolved", "Resolución"),
                            violation.resolvedAt?.let { formatSampleDate(it, language) }
                                ?: tr(language, "Not resolved", "Sin resolver"))
                        ComplianceInfoRow(tr(language, "Duration", "Duración"),
                            violation.durationMinutes?.let { "$it min" }
                                ?: tr(language, "Ongoing", "En curso"))
                        ComplianceInfoRow(tr(language, "Linked incident", "Incidente vinculado"),
                            violation.incidentReference ?: tr(language, "None", "Ninguno"))
                        Text(tr(language, violation.explanationEn, violation.explanationEs))
                    }
                }
            }
            if (type == ComplianceDetail.EVALUATION || type == ComplianceDetail.VIOLATION) {
                item {
                    OutlinedButton(onClick = { onOpenRule(rule.id) }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)) {
                        Text(tr(language, "Open environmental rule", "Abrir regla ambiental"))
                    }
                }
            }
            if (type == ComplianceDetail.VIOLATION) {
                item {
                    OutlinedButton(onClick = { onOpenEvaluation(evaluation.id) }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)) {
                        Text(tr(language, "Open associated evaluation", "Abrir evaluación asociada"))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) SafeLabPrimary else MaterialTheme.colorScheme.surfaceVariant) {
        Text(label, modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun EmptyComplianceResults() {
    val language = LocalAppLanguage.current
    CompliancePanel {
        Text(tr(language, "No matching records", "No se encontraron registros"), fontWeight = FontWeight.Bold)
        Text(tr(language, "Try another filter or search term.", "Prueba otro filtro o término de búsqueda."),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
