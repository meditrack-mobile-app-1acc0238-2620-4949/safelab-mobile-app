package pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.EnvironmentalEvaluation
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.EnvironmentalRule
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.EvaluationStatus
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.domain.model.ViolationSeverity
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.data.repository.EnvironmentalComplianceRepository
import pe.edu.upc.safelab.common.tr
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CompliancePanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .14f)),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun ComplianceHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ComplianceBadge(text: String, isGood: Boolean) {
    val accent = if (isGood) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Surface(color = accent.copy(alpha = 0.11f), shape = RoundedCornerShape(999.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent)
    }
}

@Composable
fun ComplianceMetric(label: String, value: String, detail: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .14f))) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(modifier = Modifier.size(9.dp).background(accent, CircleShape))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ComplianceInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top) {
        Text(label, modifier = Modifier.weight(.95f), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, modifier = Modifier.weight(1.25f), style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ComplianceRuleRow(rule: EnvironmentalRule, language: String, onClick: () -> Unit) {
    val evaluation = EnvironmentalComplianceRepository.latestEvaluation(rule.id)
    val compliant = evaluation?.let { EnvironmentalComplianceRepository.status(it) == EvaluationStatus.COMPLIANT } ?: false
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(17.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .14f))) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(tr(language, rule.nameEn, rule.nameEs), style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(rule.equipment, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ComplianceBadge(if (compliant) tr(language, "Compliant", "Conforme") else
                    tr(language, "Violation", "Incumplimiento"), compliant)
            }
            ComplianceInfoRow(tr(language, "Allowed range", "Rango permitido"),
                "${formatNumber(rule.minimum)} – ${formatNumber(rule.maximum)} ${rule.unit}")
            ComplianceInfoRow(tr(language, "Sample reading", "Lectura de ejemplo"),
                evaluation?.let { "${formatNumber(it.measuredValue)} ${rule.unit}" } ?: "—")
            Text(tr(language, "View rule details  ›", "Ver detalle de la regla  ›"),
                style = MaterialTheme.typography.labelMedium, color = SafeLabPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ComplianceEvaluationRow(evaluation: EnvironmentalEvaluation, language: String, onClick: () -> Unit) {
    val repo = EnvironmentalComplianceRepository
    val rule = repo.rule(evaluation.ruleId) ?: return
    val compliant = repo.status(evaluation) == EvaluationStatus.COMPLIANT
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(17.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .14f))) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(tr(language, rule.nameEn, rule.nameEs), style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Text("${evaluation.id} · ${formatSampleDate(evaluation.observedAt, language)}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ComplianceBadge(if (compliant) tr(language, "Compliant", "Conforme") else
                    tr(language, "Violation", "Incumplimiento"), compliant)
            }
            ComplianceInfoRow(tr(language, "Measured / allowed", "Medido / permitido"),
                "${formatNumber(evaluation.measuredValue)} ${rule.unit}  /  ${formatNumber(rule.minimum)}–${formatNumber(rule.maximum)} ${rule.unit}")
            Text(tr(language, "View evaluation  ›", "Ver evaluación  ›"),
                style = MaterialTheme.typography.labelMedium, color = SafeLabPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ComplianceSeverityBadge(severity: ViolationSeverity, language: String) {
    val critical = severity == ViolationSeverity.CRITICAL
    val color = if (critical) MaterialTheme.colorScheme.error else Color(0xFFD97706)
    Surface(color = color.copy(alpha = .12f), shape = RoundedCornerShape(999.dp)) {
        Text(if (critical) tr(language, "Critical", "Crítica") else tr(language, "Warning", "Advertencia"),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = color,
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

fun formatNumber(value: Double): String = if (value == value.toLong().toDouble()) value.toLong().toString()
    else String.format(Locale.US, "%.1f", value)

fun formatSampleDate(value: String, language: String): String = runCatching {
    val date = LocalDateTime.parse(value)
    val locale = if (language == "ES") Locale("es", "PE") else Locale.ENGLISH
    val pattern = if (language == "ES") "dd/MM/yyyy · HH:mm" else "MMM d, yyyy · HH:mm"
    date.format(DateTimeFormatter.ofPattern(pattern, locale))
}.getOrElse { value }
