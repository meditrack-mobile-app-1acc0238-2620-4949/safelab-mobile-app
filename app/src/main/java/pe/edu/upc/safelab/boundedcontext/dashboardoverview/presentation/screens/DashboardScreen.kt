package pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.data.local.DashboardMockData
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.components.AlertRow
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.components.DashboardActionButton
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.components.DashboardMetricCard
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.components.DashboardProgressRing
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.components.DashboardSectionCard
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.components.FacilityRow
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.components.RiskGrid
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.components.WorkflowStrip
import pe.edu.upc.safelab.ui.theme.SafeLabAccent
import pe.edu.upc.safelab.ui.theme.SafeLabPrimary

@Composable
fun DashboardScreen(
    onOpenTrends: () -> Unit = {}
) {
    val metrics = DashboardMockData.metrics

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            DashboardHero()
        }

        item {
            FilterSummaryCard()
        }

        items(metrics.chunked(2)) { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowMetrics.forEach { metric ->
                    DashboardMetricCard(
                        metric = metric,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowMetrics.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            DashboardSectionCard(
                eyebrow = "Metric explorer",
                title = "Health score"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Weighted operational readiness",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    DashboardProgressRing(
                        progress = 0.51f,
                        label = "51%",
                        color = SafeLabAccent
                    )
                    DashboardActionButton(
                        text = "View monitoring trends",
                        onClick = onOpenTrends,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            DashboardSectionCard(
                eyebrow = "Compliance control",
                title = "Storage compliance"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardProgressRing(
                        progress = 0.50f,
                        label = "50%",
                        color = SafeLabAccent
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SmallValueCard("Critical alerts", "2", Modifier.weight(1f))
                        SmallValueCard("Pending evidence", "1", Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            DashboardSectionCard(
                eyebrow = "Recent activity",
                title = "Priority alerts"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardMockData.priorityAlerts.forEach { alert ->
                        AlertRow(alert)
                    }
                }
            }
        }

        item {
            DashboardSectionCard(
                eyebrow = "Facility performance",
                title = "Monitored facilities"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardMockData.facilities.forEach { facility ->
                        FacilityRow(facility)
                    }
                }
            }
        }

        item {
            DashboardSectionCard(
                eyebrow = "Operational risks",
                title = "Risk snapshot"
            ) {
                RiskGrid(DashboardMockData.risks)
            }
        }

        item {
            DashboardSectionCard(
                eyebrow = "Cross-context workflow",
                title = "How modules interact"
            ) {
                Text(
                    text = "Operational chain generated from the shared SafeLab data model.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                WorkflowStrip(DashboardMockData.workflow)
            }
        }
    }
}

@Composable
private fun DashboardHero() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            SafeLabAccent.copy(alpha = 0.12f),
                            SafeLabPrimary.copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(
                    text = "DASHBOARD & OVERVIEW",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Operational dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Filter, compare and inspect the operational health of laboratories, sensors, alerts, incidents and compliance readiness.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Health score",
                            color = Color.White.copy(alpha = 0.86f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "51%",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "SafeLab Administrator",
                            color = Color.White.copy(alpha = 0.88f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSummaryCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "CURRENT VIEW",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )
            FilterValue("Facility", "All allowed facilities")
            FilterValue("Period", "Last 24 hours")
            FilterValue("Selected metric", "Health score")
        }
    }
}

@Composable
private fun FilterValue(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun SmallValueCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
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
