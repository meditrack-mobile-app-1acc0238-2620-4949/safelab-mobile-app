package pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingChoiceRow
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingScreenHeader
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingSectionTitle
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess

@Composable
fun GenerateReportScreen(
    onViewReports: () -> Unit = {}
) {
    var title by rememberSaveable { mutableStateOf("") }
    var reportType by rememberSaveable { mutableStateOf("Operational") }
    var equipmentId by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf("") }
    var endDate by rememberSaveable { mutableStateOf("") }
    var format by rememberSaveable { mutableStateOf("PDF") }
    var generatedMessage by rememberSaveable { mutableStateOf("") }

    val canGenerate = title.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ReportingScreenHeader(
                title = "Generate report",
                subtitle = "Create a report using the same fields defined in the SafeLab web version."
            )
        }

        if (generatedMessage.isNotBlank()) {
            item {
                Surface(
                    color = SafeLabSuccess.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = generatedMessage,
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(
                            onClick = onViewReports,
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Text("View reports", fontWeight = FontWeight.Bold)
                        }
                    }
                }
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
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ReportingSectionTitle(
                        title = "Report information",
                        subtitle = "Required fields are kept simple for the mobile flow."
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Report title") },
                        placeholder = { Text("Monthly compliance report") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Report type",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        ReportingChoiceRow(
                            options = listOf("Operational", "Compliance", "Audit", "Incident"),
                            selected = reportType,
                            onSelected = { reportType = it }
                        )
                    }

                    OutlinedTextField(
                        value = equipmentId,
                        onValueChange = { equipmentId = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Equipment ID") },
                        placeholder = { Text("Optional") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Start date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("End date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Format",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        ReportingChoiceRow(
                            options = listOf("PDF", "CSV", "XLSX"),
                            selected = format,
                            onSelected = { format = it }
                        )
                    }

                    Button(
                        onClick = {
                            generatedMessage = "$title was generated as $format for the mobile preview."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canGenerate,
                        colors = ButtonDefaults.buttonColors(containerColor = SafeLabPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Generate report", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
