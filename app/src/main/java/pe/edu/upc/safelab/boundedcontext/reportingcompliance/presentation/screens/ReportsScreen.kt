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
    var selectedType by rememberSaveable { mutableStateOf("All") }
    var searchText by rememberSaveable { mutableStateOf("") }
    var downloadedTitle by rememberSaveable { mutableStateOf("") }

    val visibleReports = reportPreviews.filter { report ->
        val typeMatches = selectedType == "All" || report.type == selectedType
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
                title = "Reports",
                subtitle = "Review generated operational, compliance and audit files.",
                actionLabel = "Generate report",
                onAction = onGenerateReport
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportingMetricCard(
                    label = "Generated reports",
                    value = "3",
                    detail = "Available in this preview",
                    marker = "R",
                    markerColor = SafeLabPrimary
                )
                ReportingMetricCard(
                    label = "Ready to download",
                    value = "2",
                    detail = "PDF and CSV files",
                    marker = "✓",
                    markerColor = SafeLabSuccess
                )
                ReportingMetricCard(
                    label = "Drafts",
                    value = "1",
                    detail = "Pending completion",
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
                        title = "Find a report",
                        subtitle = "Filter the files generated by SafeLab."
                    )
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Search report") },
                        placeholder = { Text("Cold chain, humidity, audit...") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    ReportingChoiceRow(
                        options = listOf("All", "Operational", "Compliance", "Audit", "Incident"),
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
                    title = "Recent reports",
                    subtitle = "${visibleReports.size} files shown"
                )
                OutlinedButton(
                    onClick = onExportData,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Export data", fontWeight = FontWeight.Bold)
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
                        text = "$downloadedTitle is ready for download.",
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
