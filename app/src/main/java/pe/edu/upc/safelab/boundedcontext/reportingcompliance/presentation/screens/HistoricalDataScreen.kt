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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingChoiceRow
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingScreenHeader
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingSectionTitle
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingTag

private data class HistoricalPreview(
    val sensor: String,
    val code: String,
    val location: String,
    val value: String,
    val status: String,
    val recordedAt: String
)

private val historicalPreviews = listOf(
    HistoricalPreview(
        sensor = "Reagent Freezer Temperature",
        code = "SEN-CLN-001",
        location = "Central Lab · Storage 1",
        value = "3.8 °C",
        status = "Normal",
        recordedAt = "Jun 19, 02:25"
    ),
    HistoricalPreview(
        sensor = "Hematology Room Humidity",
        code = "SEN-CLN-002",
        location = "Central Lab · Hematology",
        value = "46%",
        status = "Normal",
        recordedAt = "Jun 19, 02:24"
    ),
    HistoricalPreview(
        sensor = "PCR Freezer Temperature",
        code = "SEN-CLN-003",
        location = "Central Lab · PCR Room",
        value = "9.1 °C",
        status = "Out of range",
        recordedAt = "Jun 19, 03:10"
    ),
    HistoricalPreview(
        sensor = "Insulin Storage Humidity",
        code = "SEN-PHM-002",
        location = "Hospital Pharmacy · Insulin",
        value = "64%",
        status = "Out of range",
        recordedAt = "Jun 19, 04:00"
    ),
    HistoricalPreview(
        sensor = "PharmAndina Cold Room",
        code = "SEN-PHA-001",
        location = "PharmAndina · Cold Chain",
        value = "4.5 °C",
        status = "Normal",
        recordedAt = "Jun 19, 02:20"
    )
)

@Composable
fun HistoricalDataScreen() {
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedStatus by rememberSaveable { mutableStateOf("All") }

    val visibleItems = historicalPreviews.filter { item ->
        val searchMatches = item.sensor.contains(searchText, ignoreCase = true) ||
            item.code.contains(searchText, ignoreCase = true) ||
            item.location.contains(searchText, ignoreCase = true)
        val statusMatches = selectedStatus == "All" || item.status == selectedStatus
        searchMatches && statusMatches
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ReportingScreenHeader(
                title = "Historical data",
                subtitle = "Review stored sensor readings and environmental records."
            )
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
                        title = "Reading filters",
                        subtitle = "Search by sensor, code or location."
                    )
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Search readings") },
                        placeholder = { Text("Freezer, SEN-CLN-001...") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    ReportingChoiceRow(
                        options = listOf("All", "Normal", "Out of range"),
                        selected = selectedStatus,
                        onSelected = { selectedStatus = it }
                    )
                }
            }
        }

        item {
            ReportingSectionTitle(
                title = "Latest stored readings",
                subtitle = "${visibleItems.size} records shown"
            )
        }

        items(visibleItems, key = { it.code }) { item ->
            HistoricalCard(item)
        }
    }
}

@Composable
private fun HistoricalCard(item: HistoricalPreview) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = item.sensor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${item.code} · ${item.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ReportingTag(
                    text = item.status,
                    type = if (item.status == "Normal") "success" else "danger"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Reading",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = item.recordedAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
