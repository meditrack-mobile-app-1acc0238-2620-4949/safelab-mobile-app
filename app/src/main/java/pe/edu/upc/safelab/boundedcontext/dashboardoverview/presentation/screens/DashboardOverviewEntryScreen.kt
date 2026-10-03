package pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

private enum class DashboardDestination {
    OVERVIEW,
    TRENDS
}

@Composable
fun DashboardOverviewEntryScreen() {
    var destination by rememberSaveable { mutableStateOf(DashboardDestination.OVERVIEW) }

    when (destination) {
        DashboardDestination.OVERVIEW -> DashboardScreen(
            onOpenTrends = { destination = DashboardDestination.TRENDS }
        )
        DashboardDestination.TRENDS -> MonitoringTrendsScreen(
            onBack = { destination = DashboardDestination.OVERVIEW }
        )
    }
}
