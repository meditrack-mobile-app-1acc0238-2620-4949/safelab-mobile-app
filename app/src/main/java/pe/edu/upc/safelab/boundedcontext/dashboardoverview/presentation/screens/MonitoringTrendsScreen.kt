package pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.data.local.DashboardMockData
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.components.DashboardProgressRing
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.components.DashboardSectionCard
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.components.TrendChart
import pe.edu.upc.safelab.ui.theme.SafeLabAccent

@Composable
fun MonitoringTrendsScreen(
    onBack: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack) {
                    Text("← Back to dashboard", fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Monitoring trends",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Static TB1 preview of the dashboard trend experience. Real backend telemetry will be integrated in a later milestone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            DashboardSectionCard(
                eyebrow = "Last 24 hours",
                title = "Operational health trend"
            ) {
                TrendChart(points = DashboardMockData.healthTrend)
            }
        }

        item {
            DashboardSectionCard(
                eyebrow = "Current readiness",
                title = "Health score"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardProgressRing(
                        progress = 0.51f,
                        label = "51%",
                        color = SafeLabAccent
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Interpretation",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "The current mock dataset indicates moderate operational readiness. Critical alerts and pending evidence are the main contributors to the score.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            DashboardSectionCard(
                eyebrow = "Telemetry",
                title = "Coverage summary"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryBox("Online sensors", "9 / 10", Modifier.weight(1f))
                    SummaryBox("Coverage", "90%", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SummaryBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black
            )
        }
    }
}
