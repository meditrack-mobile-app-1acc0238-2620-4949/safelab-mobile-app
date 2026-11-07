package pe.edu.upc.safelab.boundedcontext.reportinganalytics.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.data.repository.EnvironmentalComplianceRepository
import pe.edu.upc.safelab.boundedcontext.reportinganalytics.presentation.components.ReportingMetricCard
import pe.edu.upc.safelab.boundedcontext.reportinganalytics.presentation.components.ReportingScreenHeader
import pe.edu.upc.safelab.boundedcontext.reportinganalytics.presentation.components.ReportingSectionTitle
import pe.edu.upc.safelab.common.tr
import pe.edu.upc.safelab.ui.theme.SafeLabDanger
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess

/** Reporting uses a read-only compliance snapshot, without owning or editing environmental rules. */
@Composable
fun ComplianceScreen(
    onGenerateReport: () -> Unit = {},
    onOpenEnvironmentalCompliance: () -> Unit = {}
) {
    val summary = EnvironmentalComplianceRepository.summary()
    val en = tr("Compliance reporting", "Reportes de cumplimiento")
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ReportingScreenHeader(
                title = en,
                subtitle = tr("Use the Environmental Compliance module to inspect rules and evidence.",
                    "Consulta las reglas y evidencias en el módulo de Cumplimiento Ambiental."),
                actionLabel = tr("Generate compliance report", "Generar reporte de cumplimiento"),
                onAction = onGenerateReport
            )
        }
        item {
            ReportingMetricCard(
                label = tr("Compliance snapshot", "Resumen de cumplimiento"),
                value = "${summary.compliancePercent}%",
                detail = tr("${summary.compliantRules} of ${summary.assessedRules} sample rules compliant",
                    "${summary.compliantRules} de ${summary.assessedRules} reglas de ejemplo conformes"),
                marker = "✓",
                markerColor = SafeLabSuccess
            )
        }
        item {
            ReportingMetricCard(
                label = tr("Active violations", "Incumplimientos activos"),
                value = summary.activeViolations.toString(),
                detail = tr("Offline demonstration records", "Registros de demostración sin conexión"),
                marker = "!",
                markerColor = SafeLabDanger
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportingSectionTitle(
                    title = tr("Environmental rule management", "Gestión de reglas ambientales"),
                    subtitle = tr("Rules, evaluations and violations are managed by their own Bounded Context.",
                        "Las reglas, evaluaciones e incumplimientos pertenecen a su propio Bounded Context.")
                )
                OutlinedButton(onClick = onOpenEnvironmentalCompliance, modifier = Modifier.fillMaxWidth()) {
                    Text(tr("Open environmental compliance", "Abrir cumplimiento ambiental"),
                        fontWeight = FontWeight.Bold)
                }
                Text(tr("This summary shows sample data, not live compliance certification.",
                    "Este resumen muestra datos de ejemplo, no una certificación en tiempo real."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
