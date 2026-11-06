package pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.screens

import pe.edu.upc.safelab.common.tr
import pe.edu.upc.safelab.common.LocalAppLanguage
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingChoiceRow
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingMetricCard
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingScreenHeader
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingSectionTitle
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingTag
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning

private data class ReportPreview(
    val title: String,
    val type: String,
    val format: String,
    val status: String,
    val generatedAt: String
)

private val reportPreviews = listOf(
    ReportPreview(
        title = "Cold Chain Executive Summary",
        type = "Operational",
        format = "PDF",
        status = "Ready",
        generatedAt = "Jun 19, 08:30"
    ),
    ReportPreview(
        title = "Insulin Storage Humidity Evidence",
        type = "Compliance",
        format = "CSV",
        status = "Ready",
        generatedAt = "Jun 19, 08:10"
    ),
    ReportPreview(
        title = "Remote Command Execution Trace",
        type = "Audit",
        format = "PDF",
        status = "Draft",
        generatedAt = "Jun 19, 07:40"
    )
)

@Composable
fun ReportsScreen(
    onGenerateReport: () -> Unit = {},
    onExportData: () -> Unit = {}
) {
    val language = LocalAppLanguage.current
    var selectedType by rememberSaveable { mutableStateOf(tr(language, "All", "Todos")) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var downloadedTitle by rememberSaveable { mutableStateOf("") }

    val visibleReports = reportPreviews.filter { report ->
        val typeMatches = selectedType == tr(language, "All", "Todos") || report.type == selectedType || tr(language, report.type, when(report.type){"Operational"->"Operativo";"Compliance"->"Cumplimiento";"Audit"->"Auditoría";"Incident"->"Incidente"; else->report.type}) == selectedType
        val searchMatches = report.title.contains(searchText, ignoreCase = true)
        typeMatches && searchMatches
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ReportingScreenHeader(
                title = tr(language, "Reports", "Reportes"),
                subtitle = tr(language, "Review generated operational, compliance and audit files.", "Consulta reportes generados y archivos disponibles."),
                actionLabel = tr(language, "Generate report", "Generar reporte"),
                onAction = onGenerateReport
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportingMetricCard(
                    label = tr(language, "Generated reports", "Reportes generados"),
                    value = "3",
                    detail = tr(language, "Available in this preview", "Disponibles en esta vista"),
                    marker = "R",
                    markerColor = SafeLabPrimary
                )
                ReportingMetricCard(
                    label = tr(language, "Ready to download", "Listos para descargar"),
                    value = "2",
                    detail = tr(language, "PDF and CSV files", "Archivos PDF y CSV"),
                    marker = "✓",
                    markerColor = SafeLabSuccess
                )
                ReportingMetricCard(
                    label = tr(language, "Drafts", "Borradores"),
                    value = "1",
                    detail = tr(language, "Pending completion", "Pendientes de completar"),
                    marker = "D",
                    markerColor = SafeLabWarning
                )
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportingSectionTitle(
                        title = tr(language, "Find a report", "Buscar reporte"),
                        subtitle = tr(language, "Filter the files generated by SafeLab.", "Filtra los archivos generados por SafeLab.")
                    )
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(tr(language, "Search report", "Buscar reporte")) },
                        placeholder = { Text(tr(language, "Cold chain, humidity, audit...", "Cadena de frío, humedad, auditoría...")) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    ReportingChoiceRow(
                        options = listOf(tr(language, "All", "Todos"), tr(language, "Operational", "Operativo"), tr(language, "Compliance", "Cumplimiento"), tr(language, "Audit", "Auditoría"), tr(language, "Incident", "Incidente")),
                        selected = selectedType,
                        onSelected = { selectedType = it }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReportingSectionTitle(
                    title = tr(language, "Recent reports", "Reportes recientes"),
                    subtitle = tr(language, "${visibleReports.size} files shown", "${visibleReports.size} archivos mostrados")
                )
                OutlinedButton(
                    onClick = onExportData,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(tr(language, "Export data", "Exportar datos"), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (downloadedTitle.isNotBlank()) {
            item {
                Surface(
                    color = SafeLabSuccess.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = tr(language, "$downloadedTitle is ready for download.", "$downloadedTitle está listo para descargar."),
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF047857),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        items(visibleReports, key = { it.title }) { report ->
            ReportCard(
                report = report,
                onDownload = {
                    if (report.status == "Ready") {
                        downloadedTitle = report.title
                    }
                }
            )
        }
    }
}

@Composable
private fun ReportCard(
    report: ReportPreview,
    onDownload: () -> Unit
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
                        text = report.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${report.type} · ${report.generatedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ReportingTag(
                    text = report.status,
                    type = report.status
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReportingTag(text = report.format, type = "primary")
                Button(
                    onClick = onDownload,
                    enabled = report.status == "Ready",
                    colors = ButtonDefaults.buttonColors(containerColor = SafeLabPrimary),
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Text("Download", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}